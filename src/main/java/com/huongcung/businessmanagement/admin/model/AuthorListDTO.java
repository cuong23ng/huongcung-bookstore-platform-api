package com.huongcung.businessmanagement.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for author list responses (paginated)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorListDTO {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private String nationality;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

