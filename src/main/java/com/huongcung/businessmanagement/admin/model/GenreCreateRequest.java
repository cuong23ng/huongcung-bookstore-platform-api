package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new genre
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreCreateRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Long parentId; // Optional parent genre ID for hierarchical genres
}

