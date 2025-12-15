package com.huongcung.core.inventory.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for adjusting stock levels
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {
    
    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer newQuantity;
    
    @NotBlank(message = "Reason is required")
    @Size(min = 1, max = 1000, message = "Reason must be between 1 and 1000 characters")
    private String reason;
}

