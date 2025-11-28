package com.huongcung.core.catalog.model.entity;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.GenreEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.media.model.entity.BookImageEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "abstract_book")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AbstractBookEntity extends BaseEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_authors_v2",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<AuthorEntity> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_translators_v2",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "translator_id")
    )
    private List<TranslatorEntity> translators;

    @Column(name = "edition")
    private Integer edition;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "books_genres_v2",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<GenreEntity> genres;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private PublisherEntity publisher;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookImageEntity> images;

    @OneToOne(mappedBy = "abstractBook", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    private PhysicalBookEntity physicalBookInfo;

    @OneToOne(mappedBy = "abstractBook", cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    private EbookEntity ebookInfo;
}
