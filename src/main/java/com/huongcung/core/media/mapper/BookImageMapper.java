package com.huongcung.core.media.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.media.helper.FileUrlHelper;
import com.huongcung.core.media.model.domain.BookImage;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.media.model.entity.BookImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
    componentModel = "spring",
    uses = { FileUrlHelper.class }
)
public interface BookImageMapper extends EntityMapper<BookImageDTO, BookImageEntity>, DomainMapper<BookImageEntity, BookImage> {

    @Override
    @Mappings({
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl"),
            @Mapping(target = "altText", source = "altText")
    })
    BookImageDTO toDto(BookImageEntity entity);

    @Override
    @Mappings({
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl"),
            @Mapping(target = "altText", source = "altText")
    })
    BookImage toDomain(BookImageEntity entity);
}
