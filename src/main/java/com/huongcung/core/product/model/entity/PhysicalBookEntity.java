package com.huongcung.core.product.model.entity;

import com.huongcung.core.product.enumeration.CoverType;
import com.huongcung.core.common.model.entity.PriceRowEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "physical_books")
@PrimaryKeyJoinColumn(name = "book_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalBookEntity extends AbstractBookEntity {
    
    @Column(name = "isbn", unique = true)
    private String isbn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cover_type")
    private CoverType coverType;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceRowEntity> prices;
    
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
}
