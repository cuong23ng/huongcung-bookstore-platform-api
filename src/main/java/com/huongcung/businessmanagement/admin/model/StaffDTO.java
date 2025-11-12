package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.user.enumeration.StaffType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDTO {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private StaffType staffType;
    private String assignedCity;
    private Boolean isActive;
    private LocalDate hireDate;
    
    // Note: passwordHash is explicitly excluded as per AC7
}




