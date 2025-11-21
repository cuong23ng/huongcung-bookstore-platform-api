package com.huongcung.core.media.model.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookImage extends Image {
    private String url;
    private String altText;
    private Integer position;

    public boolean isCover() {
        return position == 1;
    }

    public boolean isBackCover() {
        return position == 2;
    }
}
