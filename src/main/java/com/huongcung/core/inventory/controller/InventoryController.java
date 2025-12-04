package com.huongcung.core.inventory.controller;

import com.huongcung.core.inventory.model.dto.request.StockAdjustmentRequest;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.service.InventoryService;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Unified REST controller for inventory operations
 * Handles both Admin and Store Manager inventory operations
 * Supports both /api/admin/inventory and /api/store-manager/inventory paths for backward compatibility
 */
@RestController
@RequestMapping({
    "/api/admin/inventory",
    "/api/store-manager/inventory"
})
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class InventoryController {
    
    private final InventoryService inventoryService;

    /**
     * Get stock levels with pagination and filtering
     * Admin can filter by any warehouse
     * Store Manager can only access warehouses in their assigned city
     */
    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<BaseResponse> getStockLevels(
            @PageableDefault(size = 20, page = 0) Pageable pageable,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) String availabilityStatus) {

        City cityEnum = (city != null) ? City.valueOf(city.toUpperCase()) : null;
        InventoryService.PaginatedStockLevelResponse response = 
                inventoryService.getStockLevels(pageable, cityEnum, bookTitle, availabilityStatus, warehouseId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "stockLevels", response.stockLevels(),
                        "pagination", response.pagination()
                ))
                .build());
    }
    
    /**
     * Adjust stock level quantity with audit logging
     * Admin can adjust stock for any warehouse
     * Store Manager can only adjust stock for warehouses in their assigned city
     * 
     * @param stockLevelId the stock level ID to adjust
     * @param request the adjustment request with new quantity and reason
     * @return BaseResponse with updated stock level
     */
    @PutMapping("/stock/{stockLevelId}/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<BaseResponse> adjustStock(
            @PathVariable Long stockLevelId,
            @Valid @RequestBody StockAdjustmentRequest request) {
        
        // Adjust stock (warehouseId will be extracted from stockLevel in service, which validates city match for Store Managers)
        var updatedStockLevel = inventoryService.adjustStock(stockLevelId, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(updatedStockLevel)
                .message("Stock level adjusted successfully")
                .build());
    }

    /**
     * Get audit log of stock adjustments for a stock level
     *
     * @param stockLevelId the stock level ID
     * @param pageable pagination parameters
     * @return BaseResponse with paginated adjustment history
     */
    @GetMapping("/stock/{stockLevelId}/adjustments")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<BaseResponse> getStockAdjustments(
            @PathVariable Long stockLevelId,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {

        InventoryService.PaginatedStockAdjustmentResponse response =
                inventoryService.getStockAdjustments(stockLevelId, pageable);

        return ResponseEntity.ok(BaseResponse.builder()
                .data(Map.of(
                        "adjustments", response.adjustments(),
                        "pagination", response.pagination()
                ))
                .build());
    }
}

