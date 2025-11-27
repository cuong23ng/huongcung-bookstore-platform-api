package com.huongcung.core.catalog.mapper;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.contributor.mapper.AuthorMapper;
import com.huongcung.core.contributor.mapper.GenreMapper;
import com.huongcung.core.contributor.mapper.PublisherMapper;
import com.huongcung.core.contributor.mapper.TranslatorMapper;
import com.huongcung.core.media.mapper.BookImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = { AuthorMapper.class, TranslatorMapper.class, PublisherMapper.class, BookImageMapper.class, GenreMapper.class })
public interface AbstractBookMapper extends DomainMapper<AbstractBookEntity, AbstractBook> {

    @Override
    @Mapping(target = "physicalBookInfo.abstractBook", ignore = true)
    @Mapping(target = "ebookInfo.abstractBook", ignore = true)
    AbstractBook toDomain(AbstractBookEntity entity);
}
