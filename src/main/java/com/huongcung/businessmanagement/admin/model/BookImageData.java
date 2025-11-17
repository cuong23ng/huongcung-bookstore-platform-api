package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for Base64 encoded image data
 * Used when uploading images in the same request as book creation
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookImageData extends ImageData {

    /**
     * Optional position (1 = cover, 2 = back cover, etc.)
     * If not provided, will be set based on order in the list
     */
    private Integer position;
}










