package com.huongcung.core.user.entity;

import com.huongcung.core.user.enumeration.StaffRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staff")
@DiscriminatorValue("STAFF")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffEntity extends UserEntity {
    @Column(name = "uid", unique = true)
    private String uid;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_role", nullable = false)
    private StaffRole staffRole;
    
    @Column(name = "assigned_city")
    private String assignedCity; // For store managers: Hanoi, HCMC, DaNang
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
}
