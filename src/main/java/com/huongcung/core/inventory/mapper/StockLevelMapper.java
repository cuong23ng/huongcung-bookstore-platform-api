package com.huongcung.core.inventory.mapper;

import com.huongcung.core.catalog.mapper.PhysicalBookMapper;
import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = { PhysicalBookMapper.class, WarehouseMapper.class })
public interface StockLevelMapper extends DomainMapper<StockLevelEntity, StockLevel> {
}
