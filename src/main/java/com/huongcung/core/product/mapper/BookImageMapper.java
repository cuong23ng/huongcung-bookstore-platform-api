package com.huongcung.core.product.mapper;

import com.huongcung.core.product.dto.BookImageDTO;
import com.huongcung.core.product.entity.BookImageModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface BookImageMapper extends EntityMapper<BookImageDTO, BookImageModel> {

    @Override
    @Mappings({
            @Mapping(target = "isCover", expression = "java(entity.isCover())"),
            @Mapping(target = "isBackCover", expression = "java(entity.isBackCover())")
    })
    BookImageDTO toDto(BookImageModel entity);

    @AfterMapping
    default void fillPositionFromFlags(BookImageDTO dto, @MappingTarget BookImageModel entity) {
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
