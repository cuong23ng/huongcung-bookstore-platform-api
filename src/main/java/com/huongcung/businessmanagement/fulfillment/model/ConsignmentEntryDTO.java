package com.huongcung.businessmanagement.fulfillment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for consignment entry information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsignmentEntryDTO {
    
    private Long id;
    private Long orderEntryId;
    private Long bookId;
    private String bookTitle;
    private String bookCode;
    private Integer quantity;
    private Integer shippedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}



