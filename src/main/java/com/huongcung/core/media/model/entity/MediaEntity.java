package com.huongcung.core.media.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.media.enumeration.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class MediaEntity extends BaseEntity {
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = true)
    private FileType fileType;

    @Column(name = "url", nullable = false)
    private String url;
}
