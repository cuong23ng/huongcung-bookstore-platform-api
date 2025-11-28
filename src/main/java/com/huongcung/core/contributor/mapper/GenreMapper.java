package com.huongcung.core.contributor.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.contributor.model.domain.Genre;
import com.huongcung.core.contributor.model.entity.GenreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GenreMapper extends DomainMapper<GenreEntity, Genre> {
    
    @Override
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Genre toDomain(GenreEntity entity);
}
