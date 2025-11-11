package com.huongcung.core.media.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.media.enumeration.Folder;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class MediaEntity extends BaseEntity {
    @Column(name = "file_name", nullable = false)
    String fileName;

    @Column(name = "file_type", nullable = false)
    String fileType;

    @Column(name = "folder", nullable = false)
    Folder folder;
}
