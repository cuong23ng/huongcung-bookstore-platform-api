package com.huongcung.core.media.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.media.helper.FileUrlHelper;
import com.huongcung.core.media.model.domain.Image;
import com.huongcung.core.media.model.dto.ImageDTO;
import com.huongcung.core.media.model.entity.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for Image entities and DTOs
 */
@Mapper(
    componentModel = "spring",
    uses = { FileUrlHelper.class },
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ImageMapper {
    
    /**
     * Maps ImageEntity to ImageDTO
     * Converts relative URL to full URL using FileUrlHelper
     */
    @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl")
    ImageDTO toDto(ImageEntity entity);
}
