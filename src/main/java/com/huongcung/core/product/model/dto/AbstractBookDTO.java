package com.huongcung.core.product.model.dto;

import com.huongcung.core.common.model.dto.BaseDTO;
import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.model.dto.BookImageDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

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
