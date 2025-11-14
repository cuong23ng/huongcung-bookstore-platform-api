package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Base64 encoded image data
 * Used when uploading images in the same request as book creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookImageData {
    
    /**
     * Base64 encoded image string (with or without data URI prefix)
     * Format: "data:image/jpeg;base64,/9j/4AAQSkZJRg..." or just "/9j/4AAQSkZJRg..."
     */
    @NotBlank(message = "Image data is required")
    private String base64Data;
    
    /**
     * Optional alt text for the image
     */
    private String altText;
    
    /**
     * Optional position (1 = cover, 2 = back cover, etc.)
     * If not provided, will be set based on order in the list
     */
    private Integer position;
    
    /**
     * Optional filename (if not provided, will be generated)
     */
    private String fileName;
}


