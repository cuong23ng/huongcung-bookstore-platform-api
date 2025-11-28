package com.huongcung.webstore.bookstore.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EbookData {
    private String isbn;
    private BigDecimal currentPrice;
}
