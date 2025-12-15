package com.huongcung.core.logistics.service.impl;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.logistics.repository.ConsignmentEntityRepository;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.logistics.service.LogisticsService;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ConsignmentRepository consignmentRepository;
    private final ConsignmentEntityRepository consignmentEntityRepository;
    private final StockLevelRepository stockLevelRepository;
    private final GhnService ghnService;

    @Transactional
    @Override
    public void fulfillOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        List<ConsignmentEntity> consignments = splitOrderStrategy(order);

        for (ConsignmentEntity consignment : consignments) {
            String trackingCode = ghnService.createShippingOrder(consignment);

            consignment.setTrackingNumber(trackingCode);
            consignment.setStatus(ConsignmentStatus.PENDING);
            consignmentRepository.save(consignment);
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    /**
     * Plan fulfillment - create consignments without creating shipping orders
     * This method only splits the order into consignments and reserves stock,
     * but does not create shipping orders with GHN.
     * 
     * @param orderId the order ID
     * @return list of created consignments
     */
    @Transactional
    public List<ConsignmentEntity> planFulfillment(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        List<ConsignmentEntity> consignments = splitOrderStrategy(order);

        // Set status to CREATED (not yet sent shipping order request)
        for (ConsignmentEntity consignment : consignments) {
            consignment.setStatus(ConsignmentStatus.CREATED);
            consignmentRepository.save(consignment);
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        
        return consignments;
    }

    /**
     * Create shipping order for a consignment
     * This method sends a request to GHN to create a shipping order
     * 
     * @param consignmentId the consignment ID
     * @return tracking number from GHN
     */
    @Transactional
    public String createShippingOrderForConsignment(Long consignmentId) {
        ConsignmentEntity consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Consignment not found: " + consignmentId));

        if (consignment.getStatus() != ConsignmentStatus.CREATED) {
            throw new IllegalStateException(
                    "Consignment must be in CREATED status to create shipping order. Current status: " + consignment.getStatus());
        }

        String trackingCode = ghnService.createShippingOrder(consignment);
        consignment.setTrackingNumber(trackingCode);

        consignment.setStatus(ConsignmentStatus.PENDING);
        consignmentRepository.save(consignment);

        return trackingCode;
    }

    @Override
    public void updateConsignmentStatusByTrackingNumber(String trackingNumber, ConsignmentStatus status) {
        ConsignmentEntity consignment = consignmentRepository.findByTrackingNumber(trackingNumber).orElse(null);

        if (consignment == null) {
            log.warn("Tracking number not found: {}", trackingNumber);
            return;
        }
        consignment.setStatus(status);
        consignmentRepository.save(consignment);
        updateParentOrderStatus(consignment.getOrder());
    }

    /**
     * Split order into consignments based on stock availability and optimization
     * Algorithm: Greedy approach - group items by warehouse with available stock
     * 
     * @param order the order to split
     * @return list of consignments
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ConsignmentEntity> splitOrderStrategy(OrderEntity order) {
        log.info("Splitting order {} into consignments", order.getOrderNumber());
        
        if (order.getEntries() == null || order.getEntries().isEmpty()) {
            throw new IllegalArgumentException("Order has no entries");
        }
        
        // Get all warehouses
        List<WarehouseEntity> allWarehouses = warehouseRepository.findAll();
        if (allWarehouses.isEmpty()) {
            throw new IllegalStateException("No warehouses found in system");
        }
        
        // Map to store items allocated to each warehouse
        Map<Long, List<OrderEntryAllocation>> warehouseAllocations = new HashMap<>();
        
        // For each order entry, find the best warehouse with available stock
        for (OrderEntryEntity orderEntry : order.getEntries()) {
            // Only process physical books
            if (orderEntry.getBook() == null || orderEntry.getBook().getId() == null) {
                log.warn("Skipping order entry with null book: {}", orderEntry.getId());
                continue;
            }
            
            Long bookId = orderEntry.getBook().getId();
            Integer requestedQuantity = orderEntry.getQuantity();
            
            // Find warehouse with available stock for this book
            WarehouseEntity bestWarehouse = findBestWarehouseForBook(bookId, requestedQuantity, allWarehouses);
            
            if (bestWarehouse == null) {
                log.warn("No available stock for book {} (quantity: {}) in any warehouse", bookId, requestedQuantity);
                // Still create consignment but mark as unavailable - handle edge case
                // For now, use first warehouse as fallback
                bestWarehouse = allWarehouses.get(0);
            }
            
            warehouseAllocations.computeIfAbsent(bestWarehouse.getId(), k -> new ArrayList<>())
                    .add(new OrderEntryAllocation(orderEntry, requestedQuantity));
        }
        
        // Create consignments for each warehouse
        List<ConsignmentEntity> consignments = new ArrayList<>();
        int consignmentIndex = 1;
        
        for (Map.Entry<Long, List<OrderEntryAllocation>> entry : warehouseAllocations.entrySet()) {
            Long warehouseId = entry.getKey();
            List<OrderEntryAllocation> allocations = entry.getValue();
            
            WarehouseEntity warehouse = allWarehouses.stream()
                    .filter(w -> w.getId().equals(warehouseId))
                    .findFirst()
                    .orElseThrow();
            
            ConsignmentEntity consignment = createConsignment(order, warehouse, consignmentIndex++);
            consignment = consignmentRepository.save(consignment);
            
            List<ConsignmentEntryEntity> consignmentEntries = new ArrayList<>();
            BigDecimal consignmentTotalPrice = BigDecimal.ZERO;
            
            for (OrderEntryAllocation allocation : allocations) {
                OrderEntryEntity orderEntry = allocation.orderEntry;
                Integer quantity = allocation.quantity;
                
                // Reserve stock for this consignment entry
                reserveStockForConsignment(orderEntry.getBook().getId(), warehouseId, quantity);
                
                ConsignmentEntryEntity consignmentEntry = new ConsignmentEntryEntity();
                consignmentEntry.setOrderEntry(orderEntry);
                consignmentEntry.setConsignment(consignment);
                consignmentEntry.setQuantity(quantity);
                consignmentEntry.setShippedQuantity(0);
                consignmentEntries.add(consignmentEntry);
                
                // Calculate proportional price for this consignment entry
                BigDecimal proportionalPrice = orderEntry.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantity));
                consignmentTotalPrice = consignmentTotalPrice.add(proportionalPrice);
            }
            
            consignment.setSubTotal(consignmentTotalPrice);
            
            // Set COD amount if payment method is COD
            if (order.getPaymentMethod() == PaymentMethod.COD) {
                consignment.setCodAmount(consignmentTotalPrice);
            }
            consignment.setEntries(consignmentEntries);
            consignmentEntityRepository.saveAll(consignmentEntries);
            consignments.add(consignment);
        }
        
        log.info("Order {} split into {} consignments", order.getOrderNumber(), consignments.size());
        return consignments;
    }
    
    /**
     * Find the best warehouse for a book based on stock availability
     * Strategy: Prefer warehouse with highest available stock
     */
    private WarehouseEntity findBestWarehouseForBook(Long bookId, Integer requestedQuantity, List<WarehouseEntity> warehouses) {
        WarehouseEntity bestWarehouse = null;
        int maxAvailableQuantity = 0;
        
        for (WarehouseEntity warehouse : warehouses) {
            Optional<StockLevelEntity> stockLevelOpt = stockLevelRepository
                    .findByBookAbstractBookIdAndWarehouseId(bookId, warehouse.getId());
            
            if (stockLevelOpt.isPresent()) {
                StockLevelEntity stockLevel = stockLevelOpt.get();
                int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
                
                if (availableQuantity >= requestedQuantity && availableQuantity > maxAvailableQuantity) {
                    maxAvailableQuantity = availableQuantity;
                    bestWarehouse = warehouse;
                }
            }
        }
        
        return bestWarehouse;
    }
    
    /**
     * Reserve stock for a consignment entry
     */
    private void reserveStockForConsignment(Long bookId, Long warehouseId, Integer quantity) {
        StockLevelEntity stockLevel = stockLevelRepository
                .findByBookIdAndWarehouseIdWithLock(bookId, warehouseId)
                .orElseThrow(() -> new IllegalStateException(
                        "Stock level not found for book " + bookId + " in warehouse " + warehouseId));
        
        int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
        if (availableQuantity < quantity) {
            throw new IllegalStateException(
                    String.format("Insufficient stock for book %d in warehouse %d. Available: %d, Requested: %d",
                            bookId, warehouseId, availableQuantity, quantity));
        }
        
        stockLevel.setReservedQuantity(stockLevel.getReservedQuantity() + quantity);
        stockLevelRepository.save(stockLevel);
        
        log.debug("Reserved {} units of book {} in warehouse {} for consignment",
                quantity, bookId, warehouseId);
    }
    
    /**
     * Create a consignment entity
     */
    private ConsignmentEntity createConsignment(OrderEntity order, WarehouseEntity warehouse, int index) {
        ConsignmentEntity consignment = new ConsignmentEntity();
        consignment.setOrder(order);
        consignment.setCode(order.getOrderNumber() + "_" + index);
        consignment.setShippingAddress(order.getShippingAddress());
        consignment.setOriginWarehouse(warehouse);
        consignment.setStatus(ConsignmentStatus.PENDING);
        consignment.setSubTotal(BigDecimal.ZERO);
        
        // Set COD amount if payment method is COD
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            // COD amount will be set when entries are added
            consignment.setCodAmount(BigDecimal.ZERO);
        } else {
            consignment.setCodAmount(BigDecimal.ZERO);
        }
        
        return consignment;
    }
    
    /**
     * Helper class to track order entry allocations to warehouses
     */
    private static class OrderEntryAllocation {
        final OrderEntryEntity orderEntry;
        final Integer quantity;
        
        OrderEntryAllocation(OrderEntryEntity orderEntry, Integer quantity) {
            this.orderEntry = orderEntry;
            this.quantity = quantity;
        }
    }

    private void updateParentOrderStatus(OrderEntity order) {
        List<ConsignmentEntity> allConsignments = order.getConsignments();

        boolean allDelivered = allConsignments.stream()
                .allMatch(c -> c.getStatus() == ConsignmentStatus.DELIVERED);

        if (allDelivered) {
            // Nếu đơn hàng đã giao xong hết -> Update Order thành DELIVERED
            // (Nếu có Ebook, cần check thêm logic Ebook đã gửi chưa)
            if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                log.info("Order {} is fully delivered", order.getOrderNumber());

                // TODO: Bắn Event gửi mail "Giao hàng thành công" cho khách
            }
        }

        // Có thể thêm logic: Nếu có 1 gói bị RETURNED -> Order chuyển sang trạng thái cảnh báo
    }

    public ConsignmentStatus mapGhnStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick" -> ConsignmentStatus.PENDING;
            case "picking", "picked" -> ConsignmentStatus.PICKED_UP;
            case "storing", "transporting", "sorting" -> ConsignmentStatus.IN_TRANSIT;
            case "delivering" -> ConsignmentStatus.OUT_FOR_DELIVERY;
            case "delivered" -> ConsignmentStatus.DELIVERED;
            case "delivery_fail" -> ConsignmentStatus.FAILED_DELIVERY;
            case "return", "returning", "returned" -> ConsignmentStatus.RETURNED;
            default -> null;
        };
    }
}
