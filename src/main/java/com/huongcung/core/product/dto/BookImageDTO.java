package com.huongcung.core.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BookImageDTO extends BaseDTO {
    private String url;
    private String altText;
    private int position;
    private boolean isCover;
    private boolean isBackCover;
}
