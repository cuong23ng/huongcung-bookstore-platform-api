package com.huongcung.core.media.model.entity;

import com.huongcung.core.catalog.model.entity.EbookEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ebook_files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EbookFileEntity extends MediaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book")
    private EbookEntity book;

    @Column(name = "download_count")
    private Integer downloadCount = 0;
}
