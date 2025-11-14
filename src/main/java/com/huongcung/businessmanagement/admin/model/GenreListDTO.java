package com.huongcung.businessmanagement.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for genre list responses (paginated)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreListDTO {
    private Long id;
    private String name;
    private Long parentId;
    private String parentName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

