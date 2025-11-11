package com.huongcung.core.product.model.entity;

import com.huongcung.core.common.model.entity.PriceRowEntity;
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
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceRowEntity> prices;
    
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize; // in bytes
    
    @Column(name = "file_format")
    private String fileFormat; // PDF, EPUB, MOBI
    
    @Column(name = "download_count")
    private Integer downloadCount = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}
