package com.huongcung.core.catalog.model.domain;

import com.huongcung.core.media.model.domain.EbookFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class EbookInformation {
    private AbstractBook abstractBook;
    private String isbn;
    private Date publicationDate;
    private BigDecimal currentPrice;
    private List<EbookFile> files;
}
