package com.huongcung.webstore.bookstore.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookFrontPageDTO {
    private String code;
    private String title;

    private String thumbnailUrl;

    private BigDecimal physicalPrice;
    private BigDecimal ebookPrice;

    private List<AuthorFrontPageDTO> authors;

    public BookFrontPageDTO(String code, String title, String thumbnailUrl, BigDecimal physicalPrice, BigDecimal ebookPrice) {
        this.code = code;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.physicalPrice = physicalPrice;
        this.ebookPrice = ebookPrice;
    }
}