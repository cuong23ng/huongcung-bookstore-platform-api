package com.huongcung.core.media.model.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EbookFile extends Media {
    private Integer downloadCount;
}
