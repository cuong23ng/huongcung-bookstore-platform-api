package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnProvinceDTO {
    private Integer provinceId;
    private String provinceName;
}
