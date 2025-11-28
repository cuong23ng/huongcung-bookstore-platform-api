package com.huongcung.core.order.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.order.enumeration.CustomerType;
import com.huongcung.core.user.model.entity.CustomerEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_customer")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCustomerEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;
}
