package com.huongcung.core.user.model.entity;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.user.enumeration.StaffType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staffs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffEntity extends UserEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false)
    private StaffType staffType;
    
    @Column(name = "assigned_city")
    private City assignedCity; // For store managers: Hanoi, HCMC, DaNang
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
}
