package com.huongcung.businessmanagement.fulfillment.service.impl;

import com.huongcung.businessmanagement.fulfillment.model.FulfillableItemDTO;
import com.huongcung.businessmanagement.fulfillment.model.FulfillmentQueueDTO;
import com.huongcung.businessmanagement.fulfillment.service.FulfillmentService;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.search.model.dto.PaginationInfo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
}

