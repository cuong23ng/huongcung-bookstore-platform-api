package com.huongcung.storefront.checkout.dto;

import com.huongcung.core.catalog.enumeration.BookType;
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

    private Long bookId;
    
    private String bookCode;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Item type is required")
    private BookType bookType;
}

