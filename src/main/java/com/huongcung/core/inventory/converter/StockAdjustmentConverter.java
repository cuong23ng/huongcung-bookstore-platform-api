package com.huongcung.core.inventory.converter;

import com.huongcung.core.inventory.model.domain.StockAdjustment;
import org.springframework.core.convert.converter.Converter;
import com.huongcung.core.inventory.model.dto.StockAdjustmentDTO;
import org.springframework.stereotype.Component;

@Component
public class StockAdjustmentConverter implements Converter<StockAdjustment, StockAdjustmentDTO> {

    @Override
    public StockAdjustmentDTO convert(StockAdjustment stockAdjustment) {
        StockAdjustmentDTO stockAdjustmentDTO = new StockAdjustmentDTO();
        populate(stockAdjustment, stockAdjustmentDTO);
        return stockAdjustmentDTO;
    }

    private void populate(StockAdjustment source, StockAdjustmentDTO target) {
        target.setId(source.getId());
        target.setStockLevelId(source.getStockLevel().getId());
        target.setPreviousQuantity(source.getPreviousQuantity());
        target.setNewQuantity(source.getNewQuantity());
        target.setDifference(source.getDifferentQuantity());
        target.setReason(source.getReason());
        target.setAdjustedBy(source.getAdjustedBy().getId());
        target.setAdjustedByEmail(source.getAdjustedBy().getEmail());
        target.setAdjustedAt(source.getAdjustedAt());
        target.setCreatedAt(source.getCreatedAt());
    }
}
