package com.huongcung.core.catalog.model.entity;

import com.huongcung.core.catalog.enumeration.CoverType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "physical_books")
@PrimaryKeyJoinColumn(name = "book_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalBookEntity extends BookEntity {

    @OneToOne
    @JoinColumn(name = "abstract_book")
    private AbstractBookEntity abstractBook;

    @Column(name = "isbn", unique = true)
    private String isbn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type")
    private CoverType coverType;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "width_cm")
    private Integer widthCm;

    @Column(name = "length_cm")
    private Integer lengthCm;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;
}
