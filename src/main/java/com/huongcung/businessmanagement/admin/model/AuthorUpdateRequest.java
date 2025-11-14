package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing author
 * All fields are optional for partial updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorUpdateRequest {
    
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Biography must not exceed 5000 characters")
    private String biography;
    
    private String photoUrl;
    
    private LocalDate birthDate;
    
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;
}

