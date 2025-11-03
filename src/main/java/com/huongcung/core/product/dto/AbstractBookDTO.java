package com.huongcung.core.product.dto;

import com.huongcung.core.common.enumeration.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AbstractBookDTO extends BaseDTO {
    private String code;
    private String title;
    private List<AuthorDTO> authors;
    private List<TranslatorDTO> translators;
    private int edition;
    private PublisherDTO publisher;
    private Date publicationDate;
    private Language language;
    private int pageCount;
    private String description;
    private List<BookImageDTO> images;
    private boolean hasPhysicalEdition;
    private boolean hasElectricEdition;
}
