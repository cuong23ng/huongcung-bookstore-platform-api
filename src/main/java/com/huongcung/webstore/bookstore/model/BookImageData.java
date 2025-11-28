package com.huongcung.webstore.bookstore.model;

import lombok.Data;

@Data
public class BookImageData {
    private String url;
    private String altText;
    private int position;
    private boolean isCover;
    private boolean isBackCover;
}
