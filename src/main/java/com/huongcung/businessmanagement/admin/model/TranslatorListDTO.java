package com.huongcung.businessmanagement.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for translator list responses (paginated)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslatorListDTO {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

