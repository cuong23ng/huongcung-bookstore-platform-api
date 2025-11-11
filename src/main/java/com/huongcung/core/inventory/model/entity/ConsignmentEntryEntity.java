package com.huongcung.core.inventory.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consignment_entries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsignmentEntryEntity extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consignment_id", nullable = false)
    private ConsignmentEntity consignment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_entry_id", nullable = false)
    private OrderEntryEntity orderEntry;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "shipped_quantity", nullable = false)
    private Integer shippedQuantity = 0;
}
