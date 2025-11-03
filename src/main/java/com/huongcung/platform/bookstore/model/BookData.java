package com.huongcung.platform.bookstore.model;

import lombok.Data;

import java.util.List;

@Data
public class BookData {
    private String code;
    private String title;
    private List<AuthorData> authors;
    private List<TranslatorData> translators;
    private int edition;
    private PublisherData publisher;
    private String publicationDate;
    private String language;
    private int pageCount;
    private String description;
    private List<BookImageData> images;
    private boolean hasPhysicalEdition;
    private boolean hasElectricEdition;
}
