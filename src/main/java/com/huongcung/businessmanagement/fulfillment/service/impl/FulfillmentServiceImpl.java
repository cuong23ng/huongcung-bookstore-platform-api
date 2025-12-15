package com.huongcung.businessmanagement.fulfillment.service.impl;

import com.huongcung.businessmanagement.fulfillment.model.ConsignmentDTO;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentEntryDTO;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentShipRequest;
import com.huongcung.businessmanagement.fulfillment.model.FulfillableItemDTO;
import com.huongcung.businessmanagement.fulfillment.model.FulfillmentQueueDTO;
import com.huongcung.businessmanagement.fulfillment.service.FulfillmentService;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.search.model.dto.PaginationInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for fulfillment queue operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FulfillmentServiceImpl implements FulfillmentService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ConsignmentRepository consignmentRepository;
    private final OrderRepository orderRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedFulfillmentQueueResponse getFulfillmentQueue(
            City city,
            Pageable pageable,
            LocalDate fromDate,
            LocalDate toDate,
            String sortBy) {
        
        log.info("Fetching fulfillment queue - city: {}, page: {}, size: {}, fromDate: {}, toDate: {}, sortBy: {}",
                city, pageable.getPageNumber(), pageable.getPageSize(), fromDate, toDate, sortBy);
        
        // Build query for CONFIRMED orders
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderEntity> query = cb.createQuery(OrderEntity.class);
        Root<OrderEntity> root = query.from(OrderEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by status = CONFIRMED
        predicates.add(cb.equal(root.get("status"), OrderStatus.CONFIRMED));
        
        // Filter by date range
        if (fromDate != null) {
            LocalDateTime fromDateTime = fromDate.atStartOfDay();
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }
        if (toDate != null) {
            LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDateTime));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        // Apply sorting
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy.toLowerCase()) {
                case "orderdate":
                case "createdat":
                    query.orderBy(cb.asc(root.get("createdAt")));
                    break;
                case "totalamount":
                    query.orderBy(cb.asc(root.get("totalAmount")));
                    break;
                default:
                    query.orderBy(cb.asc(root.get("createdAt"))); // Default: oldest first
            }
        } else {
            query.orderBy(cb.asc(root.get("createdAt"))); // Default: oldest first
        }
        
        // Get all matching orders (we'll filter by stock availability in memory)
        List<OrderEntity> allOrders = entityManager.createQuery(query).getResultList();
        
        // Get warehouses for the city
        List<WarehouseEntity> warehouses = city != null 
                ? warehouseRepository.findByCity(city)
                : warehouseRepository.findAll();
        
        // Filter orders that have at least one fulfillable item
        List<OrderEntity> fulfillableOrders = allOrders.stream()
                .filter(order -> hasFulfillableItems(order, warehouses))
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), fulfillableOrders.size());
        List<OrderEntity> paginatedOrders = start < fulfillableOrders.size() 
                ? fulfillableOrders.subList(start, end)
                : new ArrayList<>();
        
        // Convert to DTOs
        List<FulfillmentQueueDTO> dtos = paginatedOrders.stream()
                .map(order -> toFulfillmentQueueDTO(order, warehouses))
                .collect(Collectors.toList());
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalResults((long) fulfillableOrders.size())
                .totalPages((int) Math.ceil((double) fulfillableOrders.size() / pageable.getPageSize()))
                .hasNext(end < fulfillableOrders.size())
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
        
        log.debug("Found {} fulfillable orders (page {} of {})", 
                fulfillableOrders.size(), pagination.getCurrentPage(), pagination.getTotalPages());
        
        return new PaginatedFulfillmentQueueResponse(dtos, pagination);
    }
    
    /**
     * Check if order has at least one item that can be fulfilled from the warehouses
     */
    private boolean hasFulfillableItems(OrderEntity order, List<WarehouseEntity> warehouses) {
        if (order.getEntries() == null || order.getEntries().isEmpty()) {
            return false;
        }
        
        Set<Long> warehouseIds = warehouses.stream()
                .map(WarehouseEntity::getId)
                .collect(Collectors.toSet());
        
        for (OrderEntryEntity entry : order.getEntries()) {
            // Only check physical books
            if (entry.getBook() == null || entry.getBook().getId() == null) {
                continue;
            }
            
            // Check if any warehouse has available stock for this book
            for (Long warehouseId : warehouseIds) {
                StockLevelEntity stockLevel = stockLevelRepository
                        .findByBookAbstractBookIdAndWarehouseId(entry.getBook().getId(), warehouseId)
                        .orElse(null);
                
                if (stockLevel != null) {
                    int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
                    if (availableQuantity > 0) {
                        return true; // At least one item is fulfillable
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Convert OrderEntity to FulfillmentQueueDTO
     */
    private FulfillmentQueueDTO toFulfillmentQueueDTO(OrderEntity order, List<WarehouseEntity> warehouses) {
        Set<Long> warehouseIds = warehouses.stream()
                .map(WarehouseEntity::getId)
                .collect(Collectors.toSet());
        
        List<FulfillableItemDTO> fulfillableItems = new ArrayList<>();
        int totalItemCount = order.getEntries() != null ? order.getEntries().size() : 0;
        
        if (order.getEntries() != null) {
            for (OrderEntryEntity entry : order.getEntries()) {
                if (entry.getBook() == null || entry.getBook().getId() == null) {
                    continue;
                }
                
                // Find best warehouse with available stock for this book
                for (Long warehouseId : warehouseIds) {
                    StockLevelEntity stockLevel = stockLevelRepository
                            .findByBookAbstractBookIdAndWarehouseId(entry.getBook().getId(), warehouseId)
                            .orElse(null);
                    
                    if (stockLevel != null) {
                        int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
                        if (availableQuantity > 0) {
                            fulfillableItems.add(FulfillableItemDTO.builder()
                                    .entryId(entry.getId())
                                    .bookId(entry.getBook().getId())
                                    .bookTitle(entry.getBook().getTitle())
                                    .bookCode(entry.getBook().getCode())
                                    .requestedQuantity(entry.getQuantity())
                                    .availableQuantity(availableQuantity)
                                    .unitPrice(entry.getUnitPrice())
                                    .totalPrice(entry.getTotalPrice())
                                    .build());
                            break; // Found a warehouse, move to next entry
                        }
                    }
                }
            }
        }
        
        String customerName = order.getCustomer() != null 
                ? (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()).trim()
                : "Unknown";
        
        String customerEmail = order.getCustomer() != null 
                ? order.getCustomer().getEmail()
                : null;
        
        return FulfillmentQueueDTO.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(customerName)
                .customerEmail(customerEmail)
                .orderDate(order.getCreatedAt())
                .totalAmount(order.getTotalAmount())
                .itemCount(totalItemCount)
                .fulfillableItemCount(fulfillableItems.size())
                .fulfillableItems(fulfillableItems)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedConsignmentResponse getConsignments(
            City city,
            ConsignmentStatus status,
            Pageable pageable) {
        
        log.info("Fetching consignments - city: {}, status: {}, page: {}, size: {}",
                city, status, pageable.getPageNumber(), pageable.getPageSize());
        
        // Build query using Criteria API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ConsignmentEntity> query = cb.createQuery(ConsignmentEntity.class);
        Root<ConsignmentEntity> root = query.from(ConsignmentEntity.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filter by city (warehouse city)
        if (city != null) {
            predicates.add(cb.equal(root.get("originWarehouse").get("city"), city));
        }
        
        // Filter by status (default: PENDING)
        ConsignmentStatus filterStatus = status != null ? status : ConsignmentStatus.PENDING;
        predicates.add(cb.equal(root.get("status"), filterStatus));
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("createdAt"))); // Oldest first
        
        // Get all matching consignments
        List<ConsignmentEntity> allConsignments = entityManager.createQuery(query).getResultList();
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allConsignments.size());
        List<ConsignmentEntity> paginatedConsignments = start < allConsignments.size() 
                ? allConsignments.subList(start, end)
                : new ArrayList<>();
        
        // Convert to DTOs
        List<ConsignmentDTO> dtos = paginatedConsignments.stream()
                .map(this::toConsignmentDTO)
                .collect(Collectors.toList());
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalResults((long) allConsignments.size())
                .totalPages((int) Math.ceil((double) allConsignments.size() / pageable.getPageSize()))
                .hasNext(end < allConsignments.size())
                .hasPrevious(pageable.getPageNumber() > 0)
                .build();
        
        log.debug("Found {} consignments (page {} of {})", 
                allConsignments.size(), pagination.getCurrentPage(), pagination.getTotalPages());
        
        return new PaginatedConsignmentResponse(dtos, pagination);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void shipConsignment(Long consignmentId, ConsignmentShipRequest request, Long shippedBy) {
        log.info("Shipping consignment {}, status: {}, shippedBy: {}",
                consignmentId, request.getStatus(), shippedBy);
        
        // Validate status
        if (request.getStatus() != ConsignmentStatus.PICKED_UP && 
            request.getStatus() != ConsignmentStatus.IN_TRANSIT) {
            throw new IllegalArgumentException(
                    "Consignment status must be PICKED_UP or IN_TRANSIT when shipping");
        }
        
        // Get consignment
        ConsignmentEntity consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Consignment not found: " + consignmentId));
        
        if (consignment.getStatus() != ConsignmentStatus.PENDING) {
            throw new IllegalStateException(
                    "Consignment is not in PENDING status. Current status: " + consignment.getStatus());
        }
        
        // Commit stock for each consignment entry
        commitStockForConsignment(consignment);
        
        // Update consignment
        consignment.setStatus(request.getStatus());
        consignmentRepository.save(consignment);
        
        log.info("Consignment {} shipped successfully with tracking number: {}", 
                consignmentId, consignment.getTrackingNumber());
        
        // Check if all consignments for the order are shipped
        checkAndUpdateOrderStatus(consignment.getOrder());
    }
    
    /**
     * Commit stock for a consignment (decrease quantity and reservedQuantity)
     */
    private void commitStockForConsignment(ConsignmentEntity consignment) {
        if (consignment.getEntries() == null || consignment.getEntries().isEmpty()) {
            log.warn("Consignment {} has no entries", consignment.getId());
            return;
        }
        
        WarehouseEntity warehouse = consignment.getOriginWarehouse();
        if (warehouse == null) {
            throw new IllegalStateException("Consignment has no origin warehouse");
        }
        
        for (ConsignmentEntryEntity entry : consignment.getEntries()) {
            if (entry.getOrderEntry() == null || entry.getOrderEntry().getBook() == null) {
                log.warn("Skipping consignment entry {} with null order entry or book", entry.getId());
                continue;
            }
            
            Long bookId = entry.getOrderEntry().getBook().getId();
            Integer quantity = entry.getQuantity();
            
            // Get stock level with lock
            StockLevelEntity stockLevel = stockLevelRepository
                    .findByBookIdAndWarehouseIdWithLock(bookId, warehouse.getId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Stock level not found for book " + bookId + " in warehouse " + warehouse.getId()));
            
            // Validate reserved quantity
            if (stockLevel.getReservedQuantity() < quantity) {
                throw new IllegalStateException(
                        String.format("Insufficient reserved stock for book %d in warehouse %d. Reserved: %d, Required: %d",
                                bookId, warehouse.getId(), stockLevel.getReservedQuantity(), quantity));
            }
            
            // Validate available quantity
            int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
            if (availableQuantity < quantity) {
                throw new IllegalStateException(
                        String.format("Insufficient available stock for book %d in warehouse %d. Available: %d, Required: %d",
                                bookId, warehouse.getId(), availableQuantity, quantity));
            }
            
            // Commit stock: decrease both quantity and reservedQuantity
            stockLevel.setQuantity(stockLevel.getQuantity() - quantity);
            stockLevel.setReservedQuantity(stockLevel.getReservedQuantity() - quantity);
            stockLevelRepository.save(stockLevel);
            
            log.debug("Committed {} units of book {} from warehouse {} (remaining: quantity={}, reserved={})",
                    quantity, bookId, warehouse.getId(), 
                    stockLevel.getQuantity(), stockLevel.getReservedQuantity());
        }
    }
    
    /**
     * Check if all consignments for an order are shipped and update order status
     */
    private void checkAndUpdateOrderStatus(OrderEntity order) {
        // Get all consignments for this order
        List<ConsignmentEntity> allConsignments = consignmentRepository.findAll().stream()
                .filter(c -> c.getOrder() != null && c.getOrder().getId().equals(order.getId()))
                .collect(Collectors.toList());
        
        if (allConsignments.isEmpty()) {
            log.warn("Order {} has no consignments", order.getId());
            return;
        }
        
        // Check if all consignments are shipped (PICKED_UP or IN_TRANSIT)
        boolean allShipped = allConsignments.stream()
                .allMatch(c -> c.getStatus() == ConsignmentStatus.PICKED_UP || 
                               c.getStatus() == ConsignmentStatus.IN_TRANSIT);
        
        if (allShipped && order.getStatus() != OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
            log.info("Order {} status updated to SHIPPED (all {} consignments are shipped)",
                    order.getOrderNumber(), allConsignments.size());
        }
    }
    
    /**
     * Convert ConsignmentEntity to ConsignmentDTO
     */
    private ConsignmentDTO toConsignmentDTO(ConsignmentEntity consignment) {
        OrderEntity order = consignment.getOrder();
        WarehouseEntity warehouse = consignment.getOriginWarehouse();
        
        // Get customer info from order
        String customerName = "Unknown";
        String customerEmail = null;
        if (order.getCustomer() != null) {
            customerName = (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()).trim();
            customerEmail = order.getCustomer().getEmail();
        }
        
        // Convert entries
        List<ConsignmentEntryDTO> entryDTOs = new ArrayList<>();
        if (consignment.getEntries() != null) {
            for (ConsignmentEntryEntity entry : consignment.getEntries()) {
                OrderEntryEntity orderEntry = entry.getOrderEntry();
                if (orderEntry != null && orderEntry.getBook() != null) {
                    entryDTOs.add(ConsignmentEntryDTO.builder()
                            .id(entry.getId())
                            .orderEntryId(orderEntry.getId())
                            .bookId(orderEntry.getBook().getId())
                            .bookTitle(orderEntry.getBook().getTitle())
                            .bookCode(orderEntry.getBook().getCode())
                            .quantity(entry.getQuantity())
                            .shippedQuantity(entry.getShippedQuantity())
                            .unitPrice(orderEntry.getUnitPrice())
                            .totalPrice(orderEntry.getTotalPrice())
                            .build());
                }
            }
        }
        
        return ConsignmentDTO.builder()
                .id(consignment.getId())
                .code(consignment.getCode())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(consignment.getStatus())
                .trackingNumber(consignment.getTrackingNumber())
                .estimatedDeliveryDate(consignment.getEstimatedDeliveryDate())
                .actualDeliveryDate(consignment.getActualDeliveryDate())
                .shippingAddress(consignment.getShippingAddress())
                .notes(consignment.getNotes())
                .totalPrice(consignment.getTotalPrice())
                .codAmount(consignment.getCodAmount())
                .warehouseCity(warehouse != null ? warehouse.getCity() : null)
                .warehouseId(warehouse != null ? warehouse.getId() : null)
                .warehouseCode(warehouse != null ? warehouse.getCode() : null)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .entries(entryDTOs)
                .createdAt(consignment.getCreatedAt())
                .updatedAt(consignment.getUpdatedAt())
                .build();
    }
}

