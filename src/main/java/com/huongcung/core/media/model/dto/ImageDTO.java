package com.huongcung.core.media.model.dto;

import com.huongcung.core.common.model.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ImageDTO {
    private String url;
    private String altText;
}
