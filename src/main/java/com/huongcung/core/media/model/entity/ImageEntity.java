package com.huongcung.core.media.model.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class ImageEntity extends MediaEntity {
    String altText;
}
