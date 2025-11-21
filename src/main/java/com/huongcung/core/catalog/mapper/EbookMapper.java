package com.huongcung.core.catalog.mapper;

import com.huongcung.core.catalog.model.domain.EbookInformation;
import com.huongcung.core.catalog.model.entity.EbookEntity;
import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.media.mapper.EbookFileMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {EbookFileMapper.class})
public interface EbookMapper extends DomainMapper<EbookEntity, EbookInformation> {
}
