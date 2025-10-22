package com.huongcung.core.inventory.entity;

import com.huongcung.core.product.entity.PhysicalBooksEntity;
import com.huongcung.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_levels", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "warehouse_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockLevelEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private PhysicalBooksEntity book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 5;
    
    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity = 50;
    
    @Column(name = "last_restocked")
    private LocalDateTime lastRestocked;
}
