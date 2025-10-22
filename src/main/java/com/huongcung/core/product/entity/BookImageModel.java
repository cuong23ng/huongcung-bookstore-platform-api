package com.huongcung.core.product.entity;

import com.huongcung.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "book_images")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookImageModel extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BooksEntity book;
    
    @Column(name = "url", nullable = false)
    private String url;
    
    @Column(name = "alt_text")
    private String altText;
    
    @Column(name = "position")
    private Integer position;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;
}
