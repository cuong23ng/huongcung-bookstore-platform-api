package com.huongcung.businessmanagement.admin.model;

import com.huongcung.core.common.enumeration.Language;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.product.enumeration.CoverType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed book information including all relationships
 * Used for GET /api/admin/catalog/books/{id} response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailDTO {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Language language;
    private LocalDate publicationDate;
    private Integer pageCount;
    private Integer edition;
    
    // Relationships
    private List<AuthorDTO> authors;
    private List<TranslatorDTO> translators;
    private PublisherDTO publisher;
    private List<GenreDTO> genres;
    private List<BookImageDTO> images;
    
    // Book type
    private String bookType; // "PHYSICAL" or "EBOOK"
    
    // PhysicalBookEntity specific fields
    private String isbn;
    private CoverType coverType;
    private Double weightGrams;
    private String dimensions;
    private BigDecimal currentPrice;
    
    // EbookEntity specific fields
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String fileFormat;
    private Integer downloadCount;
    
    // Common flags
    private Boolean hasPhysicalEdition;
    private Boolean hasElectricEdition;
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Simple DTO for Genre (to avoid circular dependencies)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreDTO {
        private Long id;
        private String name;
        private String description;
    }
}


