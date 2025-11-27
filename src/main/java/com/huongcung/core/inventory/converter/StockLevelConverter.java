package com.huongcung.core.inventory.converter;

import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.dto.StockLevelDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StockLevelConverter implements Converter<StockLevel, StockLevelDTO> {

    @Override
    public StockLevelDTO convert(StockLevel stockLevel) {
        StockLevelDTO stockLevelDTO = new StockLevelDTO();
        populate(stockLevel, stockLevelDTO);
        return stockLevelDTO;
    }

    private void populate(StockLevel source, StockLevelDTO target) {
        target.setId(source.getId());
        target.setBookCode(source.getBook().getAbstractBook().getCode());
        target.setBookTitle(source.getBook().getAbstractBook().getTitle());
        target.setBookId(source.getBook().getAbstractBook().getId());
        target.setBookIsbn(source.getBook().getIsbn());
        target.setWarehouseId(source.getWarehouse().getId());
        target.setWarehouseCode(source.getWarehouse().getCode());
        target.setWarehouseCity(source.getWarehouse().getCity().name());
        target.setWarehouseAddress(source.getWarehouse().getAddress());
        target.setQuantity(source.getQuantity());
        target.setReservedQuantity(source.getReservedQuantity());
        target.setAvailableQuantity(source.getAvailableQuantity());
        target.setIsLowStock(source.isLowStock());
        target.setIsOutOfStock(source.isOutOfStock());
        target.setReorderLevel(source.getReorderLevel());
        target.setReorderQuantity(source.getReorderQuantity());
        target.setLastRestocked(source.getLastRestocked());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
