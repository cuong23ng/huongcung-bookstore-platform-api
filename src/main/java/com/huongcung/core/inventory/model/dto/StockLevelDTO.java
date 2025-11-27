package com.huongcung.core.inventory.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for stock level information
 * Includes book details, warehouse info, and calculated available quantity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookCode;
    private String bookIsbn;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseCity;
    private String warehouseAddress;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity; // Calculated: quantity - reservedQuantity
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private Boolean isLowStock; // Calculated: quantity <= reorderLevel
    private Boolean isOutOfStock; // Calculated: availableQuantity <= 0
    private LocalDateTime lastRestocked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

