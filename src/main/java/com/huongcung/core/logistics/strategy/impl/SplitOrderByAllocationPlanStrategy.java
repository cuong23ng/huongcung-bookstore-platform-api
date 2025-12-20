package com.huongcung.core.logistics.strategy.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.logistics.repository.ConsignmentEntityRepository;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.logistics.strategy.SplitOrderStrategy;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.model.dto.AllocationPlanDTO;
import com.huongcung.core.order.model.dto.ShipmentGroupDTO;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class SplitOrderByAllocationPlanStrategy implements SplitOrderStrategy {

    private final ObjectMapper objectMapper;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ConsignmentRepository consignmentRepository;
    private final ConsignmentEntityRepository consignmentEntityRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ConsignmentEntity> splitOrder(OrderEntity order) {
        log.info("Splitting order {} into consignments using allocation plan", order.getOrderNumber());

        if (order.getEntries() == null || order.getEntries().isEmpty()) {
            throw new IllegalArgumentException("Order has no entries");
        }

        // Validate allocation plan exists
        if (order.getAllocationPlan() == null || order.getAllocationPlan().trim().isEmpty()) {
            throw new IllegalStateException("Order " + order.getOrderNumber() + " does not have an allocation plan");
        }

        // Deserialize allocation plan
        AllocationPlanDTO allocationPlan;
        try {
            allocationPlan = objectMapper.readValue(order.getAllocationPlan(), AllocationPlanDTO.class);
        } catch (JsonProcessingException e) {
            log.info("Failed to deserialize allocation plan for order {}: {}", order.getOrderNumber(), e.getMessage());
            throw new IllegalStateException("Invalid allocation plan format for order " + order.getOrderNumber(), e);
        }

        if (allocationPlan.getGroups() == null || allocationPlan.getGroups().isEmpty()) {
            throw new IllegalStateException("Allocation plan for order " + order.getOrderNumber() + " has no groups");
        }

        // Create a map of bookId -> OrderEntryEntity for quick lookup
        Map<Long, OrderEntryEntity> bookIdToEntryMap = order.getEntries().stream()
                .filter(entry -> entry.getBook() != null && entry.getBook().getId() != null)
                .collect(Collectors.toMap(
                        entry -> entry.getBook().getId(),
                        entry -> entry,
                        (existing, replacement) -> existing // Keep first if duplicate
                ));

        // Create consignments from allocation plan groups
        List<ConsignmentEntity> consignments = new ArrayList<>();
        int consignmentIndex = 1;

        for (ShipmentGroupDTO group : allocationPlan.getGroups()) {
            if (group.getWarehouseCode() == null || group.getBookIds() == null || group.getBookIds().isEmpty()) {
                log.warn("Skipping invalid shipment group: warehouseCode={}, bookIds={}", 
                        group.getWarehouseCode(), group.getBookIds());
                continue;
            }

            // Find warehouse by code
            WarehouseEntity warehouse = warehouseRepository.findByCode(group.getWarehouseCode())
                    .orElseThrow(() -> new IllegalStateException(
                            "Warehouse not found with code: " + group.getWarehouseCode()));

            // Find order entries for this group's bookIds
            List<OrderEntryEntity> groupEntries = new ArrayList<>();
            for (Long bookId : group.getBookIds()) {
                OrderEntryEntity entry = bookIdToEntryMap.get(bookId);
                if (entry == null) {
                    log.warn("Order entry not found for bookId {} in order {}", bookId, order.getOrderNumber());
                    continue;
                }
                groupEntries.add(entry);
            }

            if (groupEntries.isEmpty()) {
                log.warn("No valid entries found for shipment group with warehouseCode: {}", group.getWarehouseCode());
                continue;
            }

            // Create consignment
            ConsignmentEntity consignment = new ConsignmentEntity();
            consignment.setOrder(order);
            consignment.setCode(order.getOrderNumber() + "_" + consignmentIndex++);
            consignment.setShippingAddress(order.getShippingAddress());
            consignment.setOriginWarehouse(warehouse);
            consignment.setStatus(ConsignmentStatus.CREATED);
            consignment.setSubTotal(BigDecimal.ZERO);
            consignment.setTotalPrice(BigDecimal.ZERO);
            consignment.setShippingAmount(BigDecimal.ZERO);
            consignment.setCodAmount(BigDecimal.ZERO);

            consignment = consignmentRepository.save(consignment);

            // Create consignment entries and reserve stock
            List<ConsignmentEntryEntity> consignmentEntries = new ArrayList<>();
            BigDecimal consignmentTotalPrice = BigDecimal.ZERO;

            for (OrderEntryEntity orderEntry : groupEntries) {
                Integer quantity = orderEntry.getQuantity();
                Long bookId = orderEntry.getBook().getId();

                // Reserve stock
                reserveStockForConsignment(bookId, warehouse.getId(), quantity);

                // Create consignment entry
                ConsignmentEntryEntity consignmentEntry = new ConsignmentEntryEntity();
                consignmentEntry.setOrderEntry(orderEntry);
                consignmentEntry.setConsignment(consignment);
                consignmentEntry.setQuantity(quantity);
                consignmentEntry.setShippedQuantity(0);
                consignmentEntries.add(consignmentEntry);

                // Calculate proportional price
                BigDecimal proportionalPrice = orderEntry.getUnitPrice()
                        .multiply(BigDecimal.valueOf(quantity));
                consignmentTotalPrice = consignmentTotalPrice.add(proportionalPrice);
            }

            consignment.setSubTotal(consignmentTotalPrice);
            consignment.setTotalPrice(consignmentTotalPrice);

            if (order.getPaymentMethod() == PaymentMethod.COD) {
                consignment.setCodAmount(consignmentTotalPrice);
            }

            consignment.setEntries(consignmentEntries);
            consignmentEntityRepository.saveAll(consignmentEntries);
            consignments.add(consignment);
        }

        log.info("Created {} consignments for order {} from allocation plan", 
                consignments.size(), order.getOrderNumber());
        return consignments;
    }

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
}
