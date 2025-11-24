package com.huongcung.core.media.model.domain;

import com.huongcung.core.common.model.domain.BaseDomain;
import com.huongcung.core.media.enumeration.FileType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class Media extends BaseDomain {
    private String fileName;
    private FileType fileType;
    private String url;
}
