package com.huongcung.core.catalog.model.domain;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.common.model.domain.BaseDomain;
import com.huongcung.core.contributor.model.domain.Author;
import com.huongcung.core.contributor.model.domain.Genre;
import com.huongcung.core.contributor.model.domain.Publisher;
import com.huongcung.core.contributor.model.domain.Translator;
import com.huongcung.core.media.model.domain.BookImage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AbstractBook extends BaseDomain {
    private String code;
    private String title;
    private List<Author> authors;
    private List<Translator> translators;
    private List<Genre> genres;
    private int edition;
    private Publisher publisher;
    private Language language;
    private int pageCount;
    private String description;
    private List<BookImage> images;

    private EbookInformation ebookInfo;
    private PhysicalBookInformation physicalBookInfo;

    private Boolean isAvailable;

    public boolean hasPhysicalEdition() {
        return physicalBookInfo != null;
    }

    public boolean hasEbookEdition() {
        return ebookInfo != null;
    }
}
