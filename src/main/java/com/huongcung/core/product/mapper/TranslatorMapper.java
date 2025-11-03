package com.huongcung.core.product.mapper;

import com.huongcung.core.product.dto.TranslatorDTO;
import com.huongcung.core.product.entity.TranslatorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TranslatorMapper extends EntityMapper<TranslatorDTO, TranslatorEntity> {
}
