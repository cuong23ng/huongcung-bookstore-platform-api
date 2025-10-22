package com.huongcung.core.product.entity;

import java.time.LocalDate;
import java.util.List;

import com.huongcung.core.common.entity.BaseEntity;
import com.huongcung.core.common.enumeration.Language;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
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
@DiscriminatorColumn(name = "book_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BooksEntity extends BaseEntity {
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_author_id")
    private AuthorEntity mainAuthor;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_co_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    ) //TODO: instead using mapping table, I want to store their pk in one column only and seperate by comma
    private List<AuthorEntity> coAuthors;

    @Column(name = "edition")
    private Integer edition;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<GenreEntity> categories;

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
    
    //TODO: sample pageS
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
