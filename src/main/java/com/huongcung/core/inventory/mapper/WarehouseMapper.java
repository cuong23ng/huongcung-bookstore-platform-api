package com.huongcung.core.inventory.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.inventory.model.domain.Warehouse;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper extends DomainMapper<WarehouseEntity, Warehouse> {

    @Override
    @Mapping(target = "stockLevels", ignore = true)
    Warehouse toDomain(WarehouseEntity entity);
}
