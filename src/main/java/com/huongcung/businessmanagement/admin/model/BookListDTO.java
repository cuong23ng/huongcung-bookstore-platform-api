package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.common.enumeration.Language;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for book list responses (paginated)
 * Contains essential book information without full relationships
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookListDTO {
    private Long id;
    private String code;
    private String title;
    private Language language;
    private LocalDate publicationDate;
    private String bookType; // "PHYSICAL" or "EBOOK"
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


