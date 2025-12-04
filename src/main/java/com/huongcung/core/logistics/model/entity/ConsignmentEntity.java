package com.huongcung.core.logistics.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.order.model.entity.OrderEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "consignments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConsignmentEntity extends BaseEntity {

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsignmentStatus status = ConsignmentStatus.PENDING;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress; // JSON object
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "cod_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal codAmount;

    @OneToMany(mappedBy = "consignment", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ConsignmentEntryEntity> entries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity originWarehouse;
}
