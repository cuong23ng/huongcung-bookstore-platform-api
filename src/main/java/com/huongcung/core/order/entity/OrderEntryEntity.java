package com.huongcung.core.order.entity;

import com.huongcung.core.product.entity.BooksEntity;
import com.huongcung.core.common.entity.BaseEntity;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.order.enumeration.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_entry")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntryEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BooksEntity book;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "city")
    private City city; // For physical items
}
