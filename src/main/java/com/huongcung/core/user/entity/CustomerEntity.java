package com.huongcung.core.user.entity;

import com.huongcung.core.user.enumeration.CustomerTier;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "customers")
@DiscriminatorValue("CUSTOMER")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEntity extends UserEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_tier")
    private CustomerTier customerTier = CustomerTier.BRONZE;
    
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;
    
    @Column(name = "total_spent")
    private Double totalSpent = 0.0;
    
    // Relationships will be added when Order entities are complete
}
