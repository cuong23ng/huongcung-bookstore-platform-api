package com.huongcung.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String bookTitle;
    private List<String> authors;
    private Integer quantity;
    private BigDecimal subTotal;
}
