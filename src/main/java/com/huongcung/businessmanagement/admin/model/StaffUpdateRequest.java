package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.user.enumeration.StaffType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffUpdateRequest {
    
    private StaffType staffType; // Optional: cannot be ADMIN
    
    private String assignedCity; // Optional: required if staffType is STORE_MANAGER
    
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName; // Optional
    
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName; // Optional
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phone; // Optional
    
    private Boolean isActive; // Optional
}




