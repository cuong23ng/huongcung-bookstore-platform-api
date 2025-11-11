package com.huongcung.core.contributor.mapper;

import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.contributor.model.dto.PublisherDTO;
import com.huongcung.core.contributor.model.entity.PublisherEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublisherMapper extends EntityMapper<PublisherDTO, PublisherEntity> {
}
