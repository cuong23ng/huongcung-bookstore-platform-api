package com.huongcung.core.media.model.entity;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "book_images_v2")
@PrimaryKeyJoinColumn(name = "image_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookImageEntity extends ImageEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private AbstractBookEntity book;

    @Column(name = "position")
    private Integer position;
}