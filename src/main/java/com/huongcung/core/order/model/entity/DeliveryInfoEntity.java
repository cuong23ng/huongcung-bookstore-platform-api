package com.huongcung.core.order.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delivery_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryInfoEntity extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;
    
    @Column(name = "province_id")
    private Integer provinceId;
    
    @Column(name = "district_id")
    private Integer districtId;
    
    @Column(name = "ward_code")
    private String wardCode;
    
    @Column(name = "service_type_id")
    private Integer serviceTypeId;
    
    @Column(name = "service_id")
    private Integer serviceId;
    
    @Column(name = "expected_delivery_time")
    private String expectedDeliveryTime;
    
    @Column(name = "ghn_order_code")
    private String ghnOrderCode;
    
    @Column(name = "weight")
    private Integer weight; // in grams
    
    @Column(name = "length")
    private Integer length; // in cm
    
    @Column(name = "width")
    private Integer width; // in cm
    
    @Column(name = "height")
    private Integer height; // in cm
}

