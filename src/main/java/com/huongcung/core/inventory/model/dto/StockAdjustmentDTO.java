package com.huongcung.core.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for stock adjustment audit log entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentDTO {
    private Long id;
    private Long stockLevelId;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer difference;
    private String reason;
    private Long adjustedBy;
    private String adjustedByEmail; // Optional: email of the user who made the adjustment
    private LocalDateTime adjustedAt;
    private LocalDateTime createdAt;
}

