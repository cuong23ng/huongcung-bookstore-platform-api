package com.huongcung.core.catalog.model.dto;

import com.huongcung.core.contributor.model.dto.AuthorDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookFrontPageDTO {
    private String code;
    private String title;

    private String coverUrl;

    private BigDecimal physicalPrice;
    private BigDecimal ebookPrice;

    private List<AuthorDTO> authors;
}
