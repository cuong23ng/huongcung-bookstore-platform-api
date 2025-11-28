package com.huongcung.webstore.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private String bookCode;
    private String bookTitle;
    private String itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}

