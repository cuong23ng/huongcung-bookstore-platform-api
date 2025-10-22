package com.huongcung.core.product.entity;

import com.huongcung.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenreEntity extends BaseEntity {
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GenreEntity parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GenreEntity> children;
    
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private List<BooksEntity> books;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
