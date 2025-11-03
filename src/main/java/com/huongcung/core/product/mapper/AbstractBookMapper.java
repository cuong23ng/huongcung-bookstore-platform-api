package com.huongcung.core.product.mapper;

import com.huongcung.core.product.dto.AbstractBookDTO;
import com.huongcung.core.product.entity.AbstractBookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring",
        uses = { AuthorMapper.class, TranslatorMapper.class, PublisherMapper.class, BookImageMapper.class })
public interface AbstractBookMapper extends EntityMapper<AbstractBookDTO, AbstractBookEntity> {
}
