package com.huongcung.core.order.entity;

import com.huongcung.core.product.entity.AbstractBookEntity;
import com.huongcung.core.common.entity.BaseEntity;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.order.enumeration.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_entries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartEntryEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private AbstractBookEntity book;
    
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
