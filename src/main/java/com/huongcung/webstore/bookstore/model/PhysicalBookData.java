package com.huongcung.webstore.bookstore.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PhysicalBookData {
    private String isbn;
    private String coverType;
    private BigDecimal currentPrice;
    private Integer weightGrams;
    private Integer heightCm;
    private Integer widthCm;
    private Integer lengthCm;
}
