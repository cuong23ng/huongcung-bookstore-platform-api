package com.huongcung.businessmanagement.admin.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {

    /**
     * Optional filename (if not provided, will be generated)
     */
    private String fileName;

    /**
     * Optional fileType (if not provided, will be generated)
     */
    private String fileType;


    /**
     * Base64 encoded image string (with or without data URI prefix)
     */
    @NotBlank(message = "Image data is required")
    private String base64Data;
}
