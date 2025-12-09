package com.huongcung.core.catalog.model.dto;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.GenreDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.model.dto.BookImageDTO;
import lombok.Data;

import java.util.List;

@Data
public class AbstractBookDTO {
    private Long id;
    private String code;
    private String title;
    private List<AuthorDTO> authors;
    private List<TranslatorDTO> translators;
    private List<GenreDTO> genres;
    private int edition;
    private PublisherDTO publisher;
    private Language language;
    private int pageCount;
    private String description;
    private List<BookImageDTO> images;
    private Boolean hasPhysicalEdition = false;
    private EbookInformationDTO ebookInfo;
    private Boolean hasEbookEdition = false;
    private PhysicalBookInformationDTO physicalBookInfo;
}
