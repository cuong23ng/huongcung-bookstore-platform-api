package com.huongcung.core.catalog.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class EbookInformationDTO {
    private String isbn;
    private Date publicationDate;
    private BigDecimal currentPrice;
}
