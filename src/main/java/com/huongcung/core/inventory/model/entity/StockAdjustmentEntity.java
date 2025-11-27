package com.huongcung.core.inventory.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.user.model.entity.StaffEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for auditing stock level adjustments
 * Records all manual adjustments made by Store Managers or Admins
 */
@Entity
@Table(name = "stock_adjustments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockAdjustmentEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_level_id", nullable = false)
    private StockLevelEntity stockLevel;
    
    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity;
    
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by", nullable = false)
    private StaffEntity adjustedBy; // User ID (StaffEntity or Admin)
    
    @Column(name = "adjusted_at", nullable = false, updatable = false)
    private LocalDateTime adjustedAt;
}

