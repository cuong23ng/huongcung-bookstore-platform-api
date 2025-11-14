package com.huongcung.platform.checkout.external.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GhnProvinceDTO {
    @JsonProperty("ProvinceID")
    private Integer provinceId;
    
    @JsonProperty("ProvinceName")
    private String provinceName;
}

