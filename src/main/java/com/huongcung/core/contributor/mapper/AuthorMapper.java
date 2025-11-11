package com.huongcung.core.contributor.mapper;

import com.huongcung.core.common.mapper.EntityMapper;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper extends EntityMapper<AuthorDTO, AuthorEntity> {
}
