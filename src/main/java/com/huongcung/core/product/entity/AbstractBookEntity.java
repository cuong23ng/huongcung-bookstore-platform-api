package com.huongcung.core.product.entity;

import java.time.LocalDate;
import java.util.List;

import com.huongcung.core.common.entity.BaseEntity;
import com.huongcung.core.common.enumeration.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AbstractBookEntity extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "books_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<AuthorEntity> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_translators",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "translator_id")
    )
    private List<TranslatorEntity> translators;

    @Column(name = "edition")
    private Integer edition;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "books_genres",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<GenreEntity> genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private PublisherEntity publisher;
    
    @Column(name = "publication_date")
    private LocalDate publicationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;
    
    @Column(name = "page_count")
    private Integer pageCount;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookImageModel> images;

    @Column(name = "has_physical_edition", nullable = false)
    private boolean hasPhysicalEdition;

    @Column(name = "has_electric_edition", nullable = false)
    private boolean hasElectricEdition;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
