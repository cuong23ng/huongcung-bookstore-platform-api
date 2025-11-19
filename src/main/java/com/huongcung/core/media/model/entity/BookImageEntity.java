package com.huongcung.core.media.model.entity;

import com.huongcung.core.catalog.model.entity.BookEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "book_images")
@PrimaryKeyJoinColumn(name = "image_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookImageEntity extends ImageEntity {
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_images_books",
        joinColumns = @JoinColumn(name = "image_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<BookEntity> books;
    
    @Column(name = "position")
    private Integer position;

    public boolean isCover() {
        return this.position != null && this.position.equals(1);
    }

    public boolean isBackCover() {
        return this.position != null && this.position.equals(2);
    }
}
