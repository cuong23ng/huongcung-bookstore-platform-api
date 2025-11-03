package com.huongcung.core.product.mapper;

import com.huongcung.core.product.dto.PublisherDTO;
import com.huongcung.core.product.entity.PublisherEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PublisherMapper extends EntityMapper<PublisherDTO, PublisherEntity> {
}
