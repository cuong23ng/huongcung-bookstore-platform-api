package com.huongcung.businessmanagement.admin.mapper;

import com.huongcung.businessmanagement.admin.model.request.BookCreateRequest;
import com.huongcung.businessmanagement.admin.model.BookDetailDTO;
import com.huongcung.core.catalog.model.dto.BookListDTO;
import com.huongcung.businessmanagement.admin.model.request.BookUpdateRequest;
import com.huongcung.core.catalog.enumeration.CoverType;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.contributor.mapper.AuthorMapper;
import com.huongcung.core.contributor.mapper.PublisherMapper;
import com.huongcung.core.contributor.mapper.TranslatorMapper;
import com.huongcung.core.media.mapper.BookImageMapper;
import com.huongcung.core.contributor.model.entity.GenreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * MapStruct mapper for Book entities and DTOs
 * Handles mapping for AbstractBookEntity with PhysicalBookEntity and EbookEntity
 */
@Mapper(
    componentModel = "spring",
    uses = { AuthorMapper.class, TranslatorMapper.class, PublisherMapper.class, BookImageMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AdminBookMapper {
    
    /**
     * Maps AbstractBookEntity to BookListDTO (for paginated lists)
     */
    @Mapping(target = "bookType", expression = "java(determineBookType(entity))")
    BookListDTO toListDTO(AbstractBookEntity entity);
    
    /**
     * Maps AbstractBookEntity to BookDetailDTO (for detailed view)
     * Handles both PhysicalBookEntity and EbookEntity
     */
    @Mapping(target = "bookType", expression = "java(determineBookType(entity))")
    @Mapping(target = "isbn", expression = "java(getIsbn(entity))")
    @Mapping(target = "coverType", expression = "java(getCoverType(entity))")
    @Mapping(target = "weightGrams", expression = "java(getWeightGrams(entity))")
    @Mapping(target = "heightCm", expression = "java(getHeightCm(entity))")
    @Mapping(target = "widthCm", expression = "java(getWidthCm(entity))")
    @Mapping(target = "lengthCm", expression = "java(getLengthCm(entity))")
    @Mapping(target = "fileUrl", expression = "java(getFileUrl(entity))")
    @Mapping(target = "fileName", expression = "java(getFileName(entity))")
    @Mapping(target = "fileSize", expression = "java(getFileSize(entity))")
    @Mapping(target = "fileFormat", expression = "java(getFileFormat(entity))")
    @Mapping(target = "downloadCount", expression = "java(getDownloadCount(entity))")
    @Mapping(target = "currentPrice", expression = "java(getCurrentPrice(entity))")
    @Mapping(target = "genres", expression = "java(mapGenres(entity.getGenres()))")
    BookDetailDTO toDetailDTO(AbstractBookEntity entity);
    
    /**
     * Maps BookCreateRequest to AbstractBookEntity
     * Note: This creates a base entity - service layer should create PhysicalBookEntity or EbookEntity
     * Relationships (authors, publishers, genres) are handled in service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Generated in service
    @Mapping(target = "authors", ignore = true) // Set in service from IDs
    @Mapping(target = "translators", ignore = true) // Set in service from IDs
    @Mapping(target = "publisher", ignore = true) // Set in service from ID
    @Mapping(target = "genres", ignore = true) // Set in service from IDs
    @Mapping(target = "images", ignore = true) // Set separately
    @Mapping(target = "physicalBookInfo", ignore = true) // Set separately
    @Mapping(target = "ebookInfo", ignore = true) // Set separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AbstractBookEntity toEntity(BookCreateRequest request);
    
    /**
     * Updates AbstractBookEntity with values from BookUpdateRequest (partial update)
     * Relationships are handled in service layer
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // Code cannot be updated
    @Mapping(target = "authors", ignore = true) // Set in service from IDs
    @Mapping(target = "translators", ignore = true) // Set in service from IDs
    @Mapping(target = "publisher", ignore = true) // Set in service from ID
    @Mapping(target = "genres", ignore = true) // Set in service from IDs
    @Mapping(target = "images", ignore = true) // Managed separately
    @Mapping(target = "physicalBookInfo", ignore = true) // Managed separately
    @Mapping(target = "ebookInfo", ignore = true) // Managed separately
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(BookUpdateRequest request, @MappingTarget AbstractBookEntity entity);
    
    // Helper methods for determining book type and extracting subtype-specific fields
    default String determineBookType(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return "PHYSICAL";
        } else if (entity.getEbookInfo() != null) {
            return "EBOOK";
        }
        return null;
    }
    
    default String getIsbn(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getIsbn();
        }
        return null;
    }
    
    default CoverType getCoverType(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getCoverType();
        }
        return null;
    }
    
    default Integer getWeightGrams(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getWeightGrams();
        }
        return null;
    }
    
    default Integer getHeightCm(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getHeightCm();
        }
        return null;
    }
    
    default Integer getWidthCm(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getWidthCm();
        }
        return null;
    }
    
    default Integer getLengthCm(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getLengthCm();
        }
        return null;
    }
    
    default String getFileUrl(AbstractBookEntity entity) {
        return null;
    }
    
    default String getFileName(AbstractBookEntity entity) {
        return null;
    }
    
    default Long getFileSize(AbstractBookEntity entity) {
        // FileSize is not stored in MediaEntity, return null or handle separately if needed
        return null;
    }
    
    default String getFileFormat(AbstractBookEntity entity) {
        return null;
    }
    
    default Integer getDownloadCount(AbstractBookEntity entity) {
        return null;
    }
    
    default java.math.BigDecimal getCurrentPrice(AbstractBookEntity entity) {
        if (entity.getPhysicalBookInfo() != null) {
            return entity.getPhysicalBookInfo().getCurrentPrice();
        } else if (entity.getEbookInfo() != null) {
            return entity.getEbookInfo().getCurrentPrice();
        }
        return null;
    }
    
    default List<BookDetailDTO.GenreDTO> mapGenres(List<GenreEntity> genres) {
        if (genres == null) {
            return null;
        }
        return genres.stream()
                .map(genre -> BookDetailDTO.GenreDTO.builder()
                        .id(genre.getId())
                        .name(genre.getCode())
                        .description(genre.getDescription())
                        .build())
                .toList();
    }
}

