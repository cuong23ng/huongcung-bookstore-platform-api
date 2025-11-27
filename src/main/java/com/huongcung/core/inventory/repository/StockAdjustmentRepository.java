package com.huongcung.core.inventory.repository;

import com.huongcung.core.inventory.model.entity.StockAdjustmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for StockAdjustmentEntity
 * Used for audit logging of stock level adjustments
 */
@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustmentEntity, Long> {
    
    /**
     * Find all adjustments for a specific stock level, ordered by most recent first
     * @param stockLevelId the stock level ID
     * @param pageable pagination parameters
     * @return page of adjustments
     */
    Page<StockAdjustmentEntity> findByStockLevelIdOrderByAdjustedAtDesc(Long stockLevelId, Pageable pageable);
    
    /**
     * Find all adjustments for a specific stock level (for non-paginated queries)
     * @param stockLevelId the stock level ID
     * @return list of adjustments ordered by most recent first
     */
    List<StockAdjustmentEntity> findByStockLevelIdOrderByAdjustedAtDesc(Long stockLevelId);
}

