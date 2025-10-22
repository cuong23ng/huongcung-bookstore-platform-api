package com.huongcung.core.order.entity;

import com.huongcung.core.common.entity.BaseEntity;
import com.huongcung.core.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartEntity extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CartEntryEntity> entries;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
