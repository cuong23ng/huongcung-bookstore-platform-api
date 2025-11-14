package com.huongcung.businessmanagement.admin.mapper;

import com.huongcung.businessmanagement.admin.model.*;
import com.huongcung.core.common.mapper.CommonMapper;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import com.huongcung.core.product.model.entity.GenreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper for Contributor entities and DTOs
 * Handles Authors, Translators, Publishers, and Genres
 */
@Mapper(
    componentModel = "spring",
    uses = { CommonMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContributorMapper {
    
    // ========== Author Mappings ==========
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuthorEntity toEntity(AuthorCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(AuthorUpdateRequest request, @MappingTarget AuthorEntity entity);
    
    AuthorListDTO toListDTO(AuthorEntity entity);
    
    AuthorDTO toDetailDTO(AuthorEntity entity);
    
    // ========== Translator Mappings ==========
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TranslatorEntity toEntity(TranslatorCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(TranslatorUpdateRequest request, @MappingTarget TranslatorEntity entity);
    
    TranslatorListDTO toListDTO(TranslatorEntity entity);
    
    TranslatorDTO toDetailDTO(TranslatorEntity entity);
    
    // ========== Publisher Mappings ==========
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PublisherEntity toEntity(PublisherCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PublisherUpdateRequest request, @MappingTarget PublisherEntity entity);
    
    PublisherListDTO toListDTO(PublisherEntity entity);
    
    PublisherDTO toDetailDTO(PublisherEntity entity);
    
    // ========== Genre Mappings ==========
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true) // Set in service from parentId
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "isActive", ignore = true) // Set to true by default
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GenreEntity toEntity(GenreCreateRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true) // Set in service from parentId
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(GenreUpdateRequest request, @MappingTarget GenreEntity entity);
    
    @Mapping(target = "parentId", expression = "java(entity.getParent() != null ? entity.getParent().getId() : null)")
    @Mapping(target = "parentName", expression = "java(entity.getParent() != null ? entity.getParent().getName() : null)")
    GenreListDTO toListDTO(GenreEntity entity);
}

