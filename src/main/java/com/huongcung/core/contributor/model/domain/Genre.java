package com.huongcung.core.contributor.model.domain;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import lombok.Data;

import java.util.List;

@Data
public class Genre {
    private String code;
    private String description;
    private Genre parent;
    private List<Genre> children;
    private List<AbstractBook> books;
}
