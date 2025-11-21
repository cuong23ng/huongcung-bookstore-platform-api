package com.huongcung.core.catalog.model.entity;

import com.huongcung.core.media.model.entity.EbookFileEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ebooks")
@PrimaryKeyJoinColumn(name = "book_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EbookEntity extends BookEntity {

    @OneToOne
    @JoinColumn(name = "abstract_book")
    private AbstractBookEntity abstractBook;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EbookFileEntity> files;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
