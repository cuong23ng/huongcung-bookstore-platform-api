package com.huongcung.core.media.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ebooks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EbookFileEntity extends MediaEntity {

    @Column(name = "download_count")
    private Integer downloadCount = 0;
}
