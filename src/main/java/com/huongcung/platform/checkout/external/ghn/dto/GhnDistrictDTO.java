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
public class GhnDistrictDTO {
    @JsonProperty("DistrictID")
    private Integer districtId;
    
    @JsonProperty("DistrictName")
    private String districtName;
    
    @JsonProperty("ProvinceID")
    private Integer provinceId;
}

