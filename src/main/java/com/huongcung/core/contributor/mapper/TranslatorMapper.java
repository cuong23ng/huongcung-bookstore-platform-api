package com.huongcung.core.contributor.mapper;

import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.contributor.model.dto.TranslatorDTO;
import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TranslatorMapper extends EntityMapper<TranslatorDTO, TranslatorEntity> {
}
