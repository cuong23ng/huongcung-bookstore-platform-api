package com.huongcung.core.contributor.mapper;

import com.huongcung.core.catalog.mapper.AbstractBookMapper;
import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.contributor.model.domain.Genre;
import com.huongcung.core.contributor.model.entity.GenreEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {AbstractBookMapper.class})
public interface GenreMapper extends DomainMapper<GenreEntity, Genre> {
}
