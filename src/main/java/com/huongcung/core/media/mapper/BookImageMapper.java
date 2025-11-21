package com.huongcung.core.media.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.media.helper.FileUrlHelper;
import com.huongcung.core.media.model.domain.BookImage;
import com.huongcung.core.media.model.dto.BookImageDTO;
import com.huongcung.core.media.model.entity.BookImageEntityv2;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(
    componentModel = "spring",
    uses = { FileUrlHelper.class }
)
public interface BookImageMapper extends EntityMapper<BookImageDTO, BookImageEntityv2>, DomainMapper<BookImageEntityv2, BookImage> {

    @Override
    @Mappings({
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl"),
            @Mapping(target = "altText", source = "altText")
    })
    BookImageDTO toDto(BookImageEntityv2 entity);

    @Override
    @Mappings({
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl"),
            @Mapping(target = "altText", source = "altText")
    })
    BookImage toDomain(BookImageEntityv2 entity);
}
