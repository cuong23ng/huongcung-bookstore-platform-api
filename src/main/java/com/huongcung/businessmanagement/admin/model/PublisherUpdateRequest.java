package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing publisher
 * All fields are optional for partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublisherUpdateRequest {
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 500, message = "Website must not exceed 500 characters")
    private String website;
}

