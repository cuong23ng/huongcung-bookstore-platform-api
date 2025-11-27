package com.huongcung.core.inventory.service.impl;

import com.huongcung.businessmanagement.inventory.model.request.StockAdjustmentRequest;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.service.InventoryService;
import com.huongcung.core.inventory.converter.StockAdjustmentConverter;
import com.huongcung.core.inventory.converter.StockLevelConverter;
import com.huongcung.core.inventory.mapper.StockAdjustmentMapper;
import com.huongcung.core.inventory.mapper.StockLevelMapper;
import com.huongcung.core.inventory.mapper.WarehouseMapper;
import com.huongcung.core.inventory.model.domain.StockAdjustment;
import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.domain.Warehouse;
import com.huongcung.core.inventory.model.dto.StockAdjustmentDTO;
import com.huongcung.core.inventory.model.dto.StockLevelDTO;
import com.huongcung.core.inventory.model.entity.StockAdjustmentEntity;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockAdjustmentRepository;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.inventory.specification.StockLevelSpecifications;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.enumeration.StaffType;
import com.huongcung.core.user.model.entity.StaffEntity;
import com.huongcung.core.user.repository.StaffRepository;
import com.huongcung.core.user.service.StaffServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation for inventory management
 * Handles stock level retrieval with city-based filtering and availability calculations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StaffServiceV2 staffServiceV2;
    private final StockLevelRepository stockLevelRepository;
    private final StockLevelMapper stockLevelMapper;
    private final WarehouseMapper warehouseMapper;
    private final StockLevelConverter stockLevelConverter;
    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StaffRepository staffRepository;
    private final StockAdjustmentMapper stockAdjustmentMapper;
    private final StockAdjustmentConverter stockAdjustmentConverter;
    private final WarehouseRepository warehouseRepository;

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id).map(warehouseMapper::toDomain).orElseThrow();
    }

    @Override
    public PaginatedStockLevelResponse getStockLevels(Pageable pageable, City city, String bookTitle, String availabilityStatus, Long warehouseId) {

        log.info("Fetching all stock levels - warehouseId: {}, page: {}, size: {}, bookTitle: {}, availabilityStatus: {}",
                warehouseId, pageable.getPageNumber(), pageable.getPageSize(), bookTitle, availabilityStatus);

        // Get current authenticated user
        CustomUserDetails userDetails = staffServiceV2.getCurrentUser();
        StaffEntity staff = staffRepository.findById(userDetails.getId()).orElseThrow();

        if (staff.getStaffType() != StaffType.ADMIN && staff.getStaffType() != StaffType.STORE_MANAGER) {
            throw new IllegalStateException("Only ADMIN or STORE_MANAGER can see the stock level.");
        }

        City chosenCity = (staff.getStaffType() == StaffType.STORE_MANAGER) ? staff.getAssignedCity() : city;

        // Query using repository - Spring automatically handles pagination and count
        Page<StockLevel> stockLevelDomainPage = findStockLevel(pageable, chosenCity, warehouseId, bookTitle, availabilityStatus);
        Page<StockLevelDTO> stockLevels = stockLevelDomainPage.map(stockLevelConverter::convert);

        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(stockLevelDomainPage.getNumber() + 1)
                .pageSize(stockLevelDomainPage.getSize())
                .totalResults(stockLevelDomainPage.getTotalElements())
                .totalPages(stockLevelDomainPage.getTotalPages())
                .hasNext(stockLevelDomainPage.hasNext())
                .hasPrevious(stockLevelDomainPage.hasPrevious())
                .build();

        return new PaginatedStockLevelResponse(stockLevels.getContent(), pagination);
    }

    @Override
    public Page<StockLevel> findStockLevel(Pageable pageable, City city, Long warehouseId, String bookTitle, String availabilityStatus) {
        // Build specification for dynamic filtering
        Specification<StockLevelEntity> spec = StockLevelSpecifications.combine(
                city, warehouseId, bookTitle, availabilityStatus
        );

        // Apply default sorting if not provided
        Pageable sortedPageable = pageable;
        if (!pageable.getSort().isSorted()) {
            // Default sort by book title (nested path)
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Order.asc("book.abstractBook.title"))
            );
        }

        return stockLevelRepository.findAll(spec, sortedPageable).map(stockLevelMapper::toDomain);
    }

    @Override
    public StockLevel findStockLevelByBookIdAndWarehouse(Long bookId, Long warehouseId) {
        return stockLevelMapper.toDomain(stockLevelRepository.findByBookAbstractBookIdAndWarehouseId(bookId, warehouseId).orElseThrow());
    }

    @Override
    @Transactional
    public void reserveBookInventory(Long bookId, Long warehouseId, Integer quantity) {
        // Use pessimistic lock to prevent race conditions
        StockLevelEntity lockedStock = stockLevelRepository
                .findByBookIdAndWarehouseIdWithLock(bookId, warehouseId)
                .orElseThrow();

        int newReserved = lockedStock.getReservedQuantity() + quantity;
        lockedStock.setReservedQuantity(newReserved);
        stockLevelRepository.save(lockedStock);

        log.info("Reserved {} units of book {} in warehouse {}",
                quantity, bookId, warehouseId);
    }
    
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public StockLevelDTO adjustStock(Long stockLevelId, StockAdjustmentRequest request) {

        // Get current authenticated user
        CustomUserDetails userDetails = staffServiceV2.getCurrentUser();

        log.info("Adjusting stock level ID: {} to quantity: {} by user: {}", 
                stockLevelId, request.getNewQuantity(), userDetails.getUsername());

        StaffEntity staff = staffRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + userDetails.getId()));

        StockLevelEntity stockLevel = stockLevelRepository.findById(stockLevelId)
                .orElseThrow(() -> new RuntimeException("Stock level not found with ID: " + stockLevelId));

        // Get warehouse from stock level
        WarehouseEntity warehouse = stockLevel.getWarehouse();
        if (warehouse == null) {
            throw new RuntimeException("Stock level does not have an associated warehouse");
        }

        if (staff.getStaffType() != StaffType.STORE_MANAGER && staff.getStaffType() != StaffType.ADMIN) {
            throw new IllegalStateException("Only ADMIN and STORE_MANAGER can adjust the stock level.");
        }

        // For Store Managers, validate they can only adjust stock for warehouses in their assigned city
        if (staff.getStaffType() == StaffType.STORE_MANAGER && staff.getAssignedCity() != null) {
            if (warehouse.getCity() != staff.getAssignedCity()) {
                throw new IllegalStateException(
                        "Store Manager can only adjust stock for warehouses in their assigned city. " +
                                "Stock level belongs to warehouse: " + stockLevel.getWarehouse().getCode() +
                                " in city: " + warehouse.getCity() +
                                ", but Store Manager is assigned to: " + staff.getAssignedCity());
            }
        }
        
        // Prevent adjusting reservedQuantity directly
        if (request.getNewQuantity() < stockLevel.getReservedQuantity()) {
            throw new IllegalArgumentException(
                    "New quantity (" + request.getNewQuantity() + ") cannot be less than reserved quantity (" + 
                    stockLevel.getReservedQuantity() + "). Reserved quantity can only be changed by order processing.");
        }
        
        // Calculate difference
        Integer previousQuantity = stockLevel.getQuantity();
        Integer newQuantity = stockLevel.getQuantity() + request.getNewQuantity();
        Integer difference = newQuantity - previousQuantity;
        
        // Update stock level quantity
        stockLevel.setQuantity(newQuantity);
        StockLevelEntity updatedStockLevel = stockLevelRepository.save(stockLevel);
        
        // Log adjustment to audit table
        StockAdjustmentEntity adjustment = StockAdjustmentEntity.builder()
                .stockLevel(stockLevel)
                .previousQuantity(previousQuantity)
                .newQuantity(newQuantity)
                .reason(request.getReason())
                .adjustedBy(staff)
                .adjustedAt(LocalDateTime.now())
                .build();
        
        stockAdjustmentRepository.save(adjustment);
        
        log.info("Stock level adjusted successfully - ID: {}, previous: {}, new: {}, difference: {}", 
                stockLevelId, previousQuantity, newQuantity, difference);
        
        // Convert to DTO and return
        StockLevel domain = stockLevelMapper.toDomain(updatedStockLevel);
        return stockLevelConverter.convert(domain);
    }

    private Page<StockAdjustment> findStockLevelById(Long stockLevelId, Pageable pageable) {
        if (!stockLevelRepository.existsById(stockLevelId)) {
            throw new RuntimeException("Stock level not found with ID: " + stockLevelId);
        }
        return stockAdjustmentRepository.findByStockLevelIdOrderByAdjustedAtDesc(stockLevelId, pageable).map(stockAdjustmentMapper::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PaginatedStockAdjustmentResponse getStockAdjustments(Long stockLevelId, Pageable pageable) {
        log.debug("Fetching stock adjustments for stock level ID: {}, page: {}, size: {}", 
                stockLevelId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<StockAdjustment> page = findStockLevelById(stockLevelId, pageable);

        Page<StockAdjustmentDTO> adjustments = page.map(stockAdjustmentConverter::convert);
        
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(adjustments.getNumber() + 1)
                .pageSize(adjustments.getSize())
                .totalResults(adjustments.getTotalElements())
                .totalPages(adjustments.getTotalPages())
                .hasNext(adjustments.hasNext())
                .hasPrevious(adjustments.hasPrevious())
                .build();
        
        return new PaginatedStockAdjustmentResponse(adjustments.getContent(), pagination);
    }
}

