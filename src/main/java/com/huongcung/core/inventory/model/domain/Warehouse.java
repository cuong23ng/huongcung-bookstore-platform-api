package com.huongcung.core.inventory.model.domain;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.common.model.domain.BaseDomain;
import lombok.Data;

import java.util.List;

@Data
public class Warehouse extends BaseDomain {
    private String code;
    private City city;
    private String address;
    private String phone;
    private String email;
    private List<StockLevel> stockLevels;
}
