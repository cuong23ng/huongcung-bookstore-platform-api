package com.huongcung.core.catalog.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookType {
    PHYSICAL("Physical"),
    EBOOK("Ebook");

    private final String code;
}
