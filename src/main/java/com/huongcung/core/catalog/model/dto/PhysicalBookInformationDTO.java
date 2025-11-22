package com.huongcung.core.catalog.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PhysicalBookInformationDTO {
    private String isbn;
    private Date publicationDate;
    private BigDecimal currentPrice;
    private String coverType;
    private Integer weightGrams;
    private Integer heightCm;
    private Integer widthCm;
    private Integer lengthCm;
}
