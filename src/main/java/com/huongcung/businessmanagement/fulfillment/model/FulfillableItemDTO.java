package com.huongcung.businessmanagement.fulfillment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for items that can be fulfilled from a warehouse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillableItemDTO {
    private Long entryId;
    private Long bookId;
    private String bookTitle;
    private String bookCode;
    private Integer requestedQuantity;
    private Integer availableQuantity; // Available stock in warehouse
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

