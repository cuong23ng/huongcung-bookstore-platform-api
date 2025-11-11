package com.huongcung.core.product.mapper;

import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.contributor.mapper.AuthorMapper;
import com.huongcung.core.contributor.mapper.PublisherMapper;
import com.huongcung.core.contributor.mapper.TranslatorMapper;
import com.huongcung.core.media.mapper.BookImageMapper;
import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = { AuthorMapper.class, TranslatorMapper.class, PublisherMapper.class, BookImageMapper.class })
public interface AbstractBookMapper extends EntityMapper<AbstractBookDTO, AbstractBookEntity> {
}
