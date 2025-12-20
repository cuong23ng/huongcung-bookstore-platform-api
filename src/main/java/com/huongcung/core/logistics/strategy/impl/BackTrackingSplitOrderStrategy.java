package com.huongcung.core.logistics.strategy.impl;

import com.huongcung.core.common.utils.AddressUtils;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.model.dto.CalculateFeeDTO;
import com.huongcung.core.logistics.model.dto.ExpectedDeliveryTimeDTO;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.logistics.repository.ConsignmentEntityRepository;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.logistics.service.DeliveryService;
import com.huongcung.core.logistics.strategy.SplitOrderStrategy;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Slf4j
public class BackTrackingSplitOrderStrategy implements SplitOrderStrategy {

    private final DeliveryService deliveryService;
    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ConsignmentRepository consignmentRepository;
    private final ConsignmentEntityRepository consignmentEntityRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<ConsignmentEntity> splitOrder(OrderEntity order) {
        log.info("Splitting order {} into consignments using backtracking", order.getOrderNumber());
        
        if (order.getEntries() == null || order.getEntries().isEmpty()) {
            throw new IllegalArgumentException("Order has no entries");
        }
        
        // Filter only physical book entries
        // Heuristic: process "harder" entries first (larger quantity -> more constrained)
        List<OrderEntryEntity> physicalEntries = order.getEntries().stream()
                .filter(entry -> entry.getBook() != null && entry.getBook().getId() != null)
                .filter(entry -> entry.getBook().getPhysicalBookInfo() != null)
                .sorted(Comparator
                        .comparingInt(OrderEntryEntity::getQuantity)
                        .reversed())
                .toList();

        if (physicalEntries.isEmpty()) {
            log.warn("Order {} has no physical book entries", order.getOrderNumber());
            return Collections.emptyList();
        }
        
        // Get all warehouses
        List<WarehouseEntity> allWarehouses = warehouseRepository.findAll();
        if (allWarehouses.isEmpty()) {
            throw new IllegalStateException("No warehouses found in system");
        }
        
        // Build initial stock map: (bookId, warehouseId) -> availableQuantity
        Map<StockKey, Integer> initialStock = new HashMap<>();
        for (OrderEntryEntity entry : physicalEntries) {
            Long bookId = entry.getBook().getId();
            for (WarehouseEntity warehouse : allWarehouses) {
                Optional<StockLevelEntity> stockLevelOpt = stockLevelRepository
                        .findByBookAbstractBookIdAndWarehouseId(bookId, warehouse.getId());
                
                if (stockLevelOpt.isPresent()) {
                    StockLevelEntity stockLevel = stockLevelOpt.get();
                    int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
                    if (availableQuantity > 0) {
                        initialStock.put(new StockKey(bookId, warehouse.getId()), availableQuantity);
                    }
                }
            }
        }
        
        // Run backtracking to find best assignment
        BestSolution bestSolution = new BestSolution();
        Map<OrderEntryEntity, WarehouseEntity> currentAssign = new HashMap<>();
        Set<WarehouseEntity> usedWarehouses = new HashSet<>();
        
        backtrack(physicalEntries, 0, allWarehouses, initialStock, currentAssign, usedWarehouses, bestSolution, order);
        
        if (bestSolution.assignment == null || bestSolution.assignment.isEmpty()) {
            log.warn("No valid assignment found for order {}", order.getOrderNumber());
            throw new IllegalStateException("Cannot fulfill order: insufficient stock in all warehouses");
        }
        
        log.info("Best solution found: {} warehouses, max delivery time: {} days, total fee: {}", 
                bestSolution.cost.warehouseCount, bestSolution.cost.maxExpectedDeliveryTime, bestSolution.cost.totalFee);
        
        // Create consignments from best assignment
        return createConsignmentsFromAssignment(order, bestSolution.assignment);
    }

    private void backtrack(
            List<OrderEntryEntity> entries,
            int idx,
            List<WarehouseEntity> warehouses,
            Map<StockKey, Integer> remainingStock,   // (bookId, warehouseId) -> qty
            Map<OrderEntryEntity, WarehouseEntity> currentAssign,
            Set<WarehouseEntity> usedWarehouses,
            BestSolution best,
            OrderEntity order
    ) {
        // Base case: all entries assigned
        if (idx == entries.size()) {
            SolutionCost cost = computeSolutionCost(usedWarehouses, currentAssign, order);
            if (best.cost == null || cost.compareTo(best.cost) < 0) {
                best.cost = cost;
                best.assignment = new HashMap<>(currentAssign);
            }
            return;
        }

        // Pruning 1: nếu đã dùng nhiều kho hơn best hiện tại thì cắt luôn
        // vì warehouseCount là tiêu chí ưu tiên số 1.
        if (best.cost != null && usedWarehouses.size() > best.cost.warehouseCount) {
            return;
        }

        OrderEntryEntity entry = entries.get(idx);
        Long bookId = entry.getBook().getId();
        int qty = entry.getQuantity();

        // Find warehouses with sufficient stock for this entry
        List<WarehouseEntity> candidateWarehouses = warehouses.stream()
                .filter(w -> {
                    int available = remainingStock.getOrDefault(new StockKey(bookId, w.getId()), 0);
                    return available >= qty;
                })
                // Heuristic: try warehouses with more available stock first
                .sorted(Comparator.comparingInt(
                        (WarehouseEntity w) -> remainingStock.getOrDefault(new StockKey(bookId, w.getId()), 0)
                ).reversed())
                .toList();

        if (candidateWarehouses.isEmpty()) {
            return;
        }

        // Try each candidate warehouse
        for (WarehouseEntity w : candidateWarehouses) {
            StockKey stockKey = new StockKey(bookId, w.getId());
            int available = remainingStock.getOrDefault(stockKey, 0);

            // Assign this warehouse to the entry
            remainingStock.put(stockKey, available - qty);
            WarehouseEntity prev = currentAssign.put(entry, w);
            boolean addedNewWarehouse = usedWarehouses.add(w);

            // Recursive call
            backtrack(entries, idx + 1, warehouses,
                    remainingStock, currentAssign, usedWarehouses, best, order);

            // Backtrack: restore state
            remainingStock.put(stockKey, available);
            if (prev != null) {
                currentAssign.put(entry, prev);
            } else {
                currentAssign.remove(entry);
            }
            if (addedNewWarehouse) {
                usedWarehouses.remove(w);
            }
        }
    }

    private SolutionCost computeSolutionCost(
            Set<WarehouseEntity> usedWarehouses,
            Map<OrderEntryEntity, WarehouseEntity> assignment,
            OrderEntity order
    ) {
        int warehouseCount = usedWarehouses.size();
        BigDecimal totalFee = BigDecimal.ZERO;
        int maxExpectedDeliveryTime = 0; // Max delivery time in days across all consignments

        // Group entries by warehouse to calculate fee per consignment
        Map<WarehouseEntity, List<OrderEntryEntity>> entriesByWarehouse = new HashMap<>();
        for (Map.Entry<OrderEntryEntity, WarehouseEntity> entry : assignment.entrySet()) {
            entriesByWarehouse.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        for (WarehouseEntity w : usedWarehouses) {
            List<OrderEntryEntity> entries = entriesByWarehouse.get(w);
            
            // 1) Calculate GHN fee for consignment from warehouse w to order.shippingAddress
            BigDecimal fee = estimateShippingFee(w, entries, order);
            totalFee = totalFee.add(fee);

            // 2) Calculate expected delivery time (in days) based on entries and warehouse
            int deliveryTime = estimateExpectedDeliveryTime(w, entries, order);
            maxExpectedDeliveryTime = Math.max(maxExpectedDeliveryTime, deliveryTime);
        }

        SolutionCost cost = new SolutionCost();
        cost.warehouseCount = warehouseCount;
        cost.totalFee = totalFee;
        cost.maxExpectedDeliveryTime = maxExpectedDeliveryTime;
        return cost;
    }
    
    private BigDecimal estimateShippingFee(WarehouseEntity warehouse, List<OrderEntryEntity> entries, OrderEntity order) {
        AddressDTO warehouseAddress = AddressUtils.parseAddressJson(warehouse.getAddress());
        AddressDTO customerAddress = AddressUtils.parseAddressJson(order.getShippingAddress());

        if (warehouseAddress == null || customerAddress == null ||
                customerAddress.getDistrict() == null || customerAddress.getWard() == null) {
            log.warn("Cannot parse addresses for fee calculation, using default fee");
            return BigDecimal.valueOf(50000); // Default fee
        }

        CalculateFeeDTO feeDTO = deliveryService.calculateEstimatedDeliveryFee(entries, warehouseAddress, customerAddress);

        return feeDTO.getTotal();
    }
    
    private int estimateExpectedDeliveryTime(WarehouseEntity warehouse, List<OrderEntryEntity> entries, OrderEntity order) {

        AddressDTO warehouseAddress = AddressUtils.parseAddressJson(warehouse.getAddress());
        AddressDTO customerAddress = AddressUtils.parseAddressJson(order.getShippingAddress());

        ExpectedDeliveryTimeDTO deliveryTimeDTO = deliveryService.calculateExpectedDeliveryTime(entries, warehouseAddress, customerAddress);

        // Parse leadTime to number of days
        if (deliveryTimeDTO != null && deliveryTimeDTO.getLeadTime() != null) {
            String leadTime = deliveryTimeDTO.getLeadTime();

            // Try to parse as integer (number of days)
            try {
                return Integer.parseInt(leadTime);
            } catch (NumberFormatException e) {
                // If it's a date string, calculate days from today
                try {
                    LocalDate deliveryDate = LocalDate.parse(leadTime, DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate today = LocalDate.now();
                    long daysBetween = ChronoUnit.DAYS.between(today, deliveryDate);
                    return (int) Math.max(0, daysBetween); // Ensure non-negative
                } catch (DateTimeParseException ex) {
                    log.warn("Cannot parse leadTime as date or number: {}, using default", leadTime);
                    return 7; // Default 7 days
                }
            }
        }

        return 7; // Default 7 days if API call fails
    }

    private List<ConsignmentEntity> createConsignmentsFromAssignment(
            OrderEntity order,
            Map<OrderEntryEntity, WarehouseEntity> assignment) {
        
        // Group entries by warehouse
        Map<WarehouseEntity, List<OrderEntryEntity>> entriesByWarehouse = new HashMap<>();
        for (Map.Entry<OrderEntryEntity, WarehouseEntity> entry : assignment.entrySet()) {
            entriesByWarehouse.computeIfAbsent(entry.getValue(), k -> new ArrayList<>())
                    .add(entry.getKey());
        }
        
        List<ConsignmentEntity> consignments = new ArrayList<>();
        int consignmentIndex = 1;
        
        for (Map.Entry<WarehouseEntity, List<OrderEntryEntity>> entry : entriesByWarehouse.entrySet()) {
            WarehouseEntity warehouse = entry.getKey();
            List<OrderEntryEntity> entries = entry.getValue();
            
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
            consignment.setCodAmount(BigDecimal.ZERO); // Will be updated when entries are added
            
            consignment = consignmentRepository.save(consignment);
            
            // Create consignment entries and reserve stock
            List<ConsignmentEntryEntity> consignmentEntries = new ArrayList<>();
            BigDecimal consignmentTotalPrice = BigDecimal.ZERO;
            
            for (OrderEntryEntity orderEntry : entries) {
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
        
        log.info("Created {} consignments for order {}", consignments.size(), order.getOrderNumber());
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
    
    // Helper classes
    private static class StockKey {
        private final Long bookId;
        private final Long warehouseId;
        
        public StockKey(Long bookId, Long warehouseId) {
            this.bookId = bookId;
            this.warehouseId = warehouseId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StockKey stockKey = (StockKey) o;
            return Objects.equals(bookId, stockKey.bookId) &&
                   Objects.equals(warehouseId, stockKey.warehouseId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(bookId, warehouseId);
        }
    }
    
    private static class SolutionCost implements Comparable<SolutionCost> {
        int warehouseCount;
        int maxExpectedDeliveryTime; // Max delivery time in days across all consignments
        BigDecimal totalFee;
        
        @Override
        public int compareTo(SolutionCost other) {
            // Priority 1: Minimize warehouse count
            if (this.warehouseCount != other.warehouseCount) {
                return Integer.compare(this.warehouseCount, other.warehouseCount);
            }
            // Priority 2: Minimize max expected delivery time (faster delivery is better)
            if (this.maxExpectedDeliveryTime != other.maxExpectedDeliveryTime) {
                return Integer.compare(this.maxExpectedDeliveryTime, other.maxExpectedDeliveryTime);
            }
            // Priority 3: Minimize total fee
            return this.totalFee.compareTo(other.totalFee);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SolutionCost that = (SolutionCost) o;
            return warehouseCount == that.warehouseCount &&
                   maxExpectedDeliveryTime == that.maxExpectedDeliveryTime &&
                   Objects.equals(totalFee, that.totalFee);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(warehouseCount, maxExpectedDeliveryTime, totalFee);
        }
    }
    
    private static class BestSolution {
        SolutionCost cost;
        Map<OrderEntryEntity, WarehouseEntity> assignment;
    }
}
