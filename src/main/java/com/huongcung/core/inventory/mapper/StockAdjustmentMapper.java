package com.huongcung.core.inventory.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.inventory.model.domain.StockAdjustment;
import com.huongcung.core.inventory.model.entity.StockAdjustmentEntity;
import com.huongcung.core.user.mapper.StaffMapperV2;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for StockAdjustmentEntity to StockAdjustmentDTO
 */
@Mapper(componentModel = "spring",
        uses = { StaffMapperV2.class, StockLevelMapper.class } )
public interface StockAdjustmentMapper extends DomainMapper<StockAdjustmentEntity, StockAdjustment> {

}

