package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new translator
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslatorCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 5000, message = "Biography must not exceed 5000 characters")
    private String biography;
    
    private String photoUrl;
    
    private LocalDate birthDate;
}

