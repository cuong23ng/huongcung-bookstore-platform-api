package com.huongcung.core.media.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BookImageDTO extends ImageDTO {
    private int position;
    private boolean isCover;
    private boolean isBackCover;
}
