package com.huongcung.core.product.model.entity;

import com.huongcung.core.common.model.entity.PriceRowEntity;
import com.huongcung.core.media.model.entity.EbookFileEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ebooks")
@PrimaryKeyJoinColumn(name = "book_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EbookEntity extends AbstractBookEntity {

    @Column(name = "isbn", unique = true)
    private String isbn;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceRowEntity> prices;
    
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file", nullable = false, unique = true)
    private EbookFileEntity file;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}
