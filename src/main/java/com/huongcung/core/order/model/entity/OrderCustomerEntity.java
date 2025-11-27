package com.huongcung.core.order.model.entity;

import com.huongcung.core.common.model.entity.BaseEntity;
import com.huongcung.core.order.enumeration.CustomerType;
import com.huongcung.core.user.model.entity.CustomerEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_customer")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCustomerEntity extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @Column(name = "email")
    private String email;

    @Column(name = "fullName")
    private String fullName;
}
