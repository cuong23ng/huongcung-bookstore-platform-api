package com.huongcung.core.inventory.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.inventory.enumeration.City;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseEntity extends BaseEntity {
    
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "city", nullable = false)
    private City city;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockLevelEntity> stockLevels;
}
