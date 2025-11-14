package com.huongcung.core.inventory.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.inventory.enumeration.ConsignmentStatus;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsignmentStatus status = ConsignmentStatus.PENDING;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "shipping_company")
    private String shippingCompany;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress; // JSON object
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "consignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ConsignmentEntryEntity> entries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity originWarehouse;
}
