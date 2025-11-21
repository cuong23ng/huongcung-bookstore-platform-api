package com.huongcung.core.catalog.mapper;

import com.huongcung.core.catalog.model.domain.PhysicalBookInformation;
import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import com.huongcung.core.common.mapper.DomainMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhysicalBookMapper extends DomainMapper<PhysicalBookEntity, PhysicalBookInformation> {
}
