package com.huongcung.core.media.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookImageEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private AbstractBookEntity book;
    
    @Column(name = "url", nullable = false)
    private String url;
    
    @Column(name = "alt_text")
    private String altText;
    
    @Column(name = "position")
    private Integer position;

    public boolean isCover() {
        return this.position != null && this.position.equals(1);
    }

    public boolean isBackCover() {
        return this.position != null && this.position.equals(2);
    }
}
