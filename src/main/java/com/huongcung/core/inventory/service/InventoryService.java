package com.huongcung.core.inventory.service;

import com.huongcung.businessmanagement.inventory.model.request.StockAdjustmentRequest;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.dto.StockAdjustmentDTO;
import com.huongcung.core.inventory.model.dto.StockLevelDTO;
import com.huongcung.core.search.model.dto.PaginationInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    PaginatedStockLevelResponse getStockLevels(Pageable pageable, City city, String bookTitle, String availabilityStatus, Long warehouseId);

    Page<StockLevel> findStockLevel(Pageable pageable, City city, Long warehouseId, String bookTitle, String availabilityStatus);

    StockLevel findStockLevelByBookIdAndWarehouse(Long bookId, Long warehouseId);

    void reserveBookInventory(Long bookId, Long warehouseId, Integer quantity);
    
    /**
     * Adjust stock level quantity with audit logging
     * @param stockLevelId the stock level ID to adjust
     * @param request the adjustment request with new quantity and reason
     * @return the updated stock level DTO
     */
    StockLevelDTO adjustStock(Long stockLevelId, StockAdjustmentRequest request);
    
    /**
     * Get paginated audit log of stock adjustments for a stock level
     * @param stockLevelId the stock level ID
     * @param pageable pagination parameters
     * @return paginated list of adjustments
     */
    PaginatedStockAdjustmentResponse getStockAdjustments(Long stockLevelId, Pageable pageable);

    record PaginatedStockLevelResponse(
            List<StockLevelDTO> stockLevels,
            PaginationInfo pagination
    ) {}
    
    record PaginatedStockAdjustmentResponse(
            List<StockAdjustmentDTO> adjustments,
            PaginationInfo pagination
    ) {}
}






