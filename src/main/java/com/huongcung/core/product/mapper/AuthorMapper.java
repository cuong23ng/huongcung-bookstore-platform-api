package com.huongcung.core.product.mapper;

import com.huongcung.core.product.dto.AuthorDTO;
import com.huongcung.core.product.entity.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AuthorMapper extends EntityMapper<AuthorDTO, AuthorEntity> {
}
