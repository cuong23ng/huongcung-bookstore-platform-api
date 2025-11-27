package com.huongcung.core.catalog.model.domain;

import com.huongcung.core.catalog.enumeration.CoverType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PhysicalBookInformation {
    private AbstractBook abstractBook;
    private String isbn;
    private Date publicationDate;
    private BigDecimal currentPrice;
    private CoverType coverType;
    private Integer weightGrams;
    private Integer heightCm;
    private Integer widthCm;
    private Integer lengthCm;
}
