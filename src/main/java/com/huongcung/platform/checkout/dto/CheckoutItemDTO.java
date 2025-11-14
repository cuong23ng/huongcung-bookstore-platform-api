package com.huongcung.platform.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemDTO {
    // Accept either bookId (for backward compatibility) or bookCode
    private Long bookId;
    
    private String bookCode; // Book code (alternative to bookId)
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @NotNull(message = "Item type is required")
    private String itemType; // PHYSICAL or DIGITAL
}

