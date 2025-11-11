package com.huongcung.core.media.mapper;

import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.media.model.entity.BookImageEntity;
import com.huongcung.core.media.helper.ImageUrlHelper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(
    componentModel = "spring",
    uses = { ImageUrlHelper.class }
)
public interface BookImageMapper extends EntityMapper<BookImageDTO, BookImageEntity> {

    @Override
    @Mappings({
            @Mapping(target = "isCover", expression = "java(entity.isCover())"),
            @Mapping(target = "isBackCover", expression = "java(entity.isBackCover())"),
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl"),
            @Mapping(target = "altText", source = "altText")
    })
    BookImageDTO toDto(BookImageEntity entity);

    @AfterMapping
    default void fillPositionFromFlags(BookImageDTO dto, @MappingTarget BookImageEntity entity) {
        if (dto == null || entity == null) return;
        // Respect explicit position if provided (> 0), otherwise derive from flags
        if (dto.getPosition() > 0) return;
        if (dto.isCover()) {
            entity.setPosition(1);
        } else if (dto.isBackCover()) {
            entity.setPosition(2);
        }
    }
}
