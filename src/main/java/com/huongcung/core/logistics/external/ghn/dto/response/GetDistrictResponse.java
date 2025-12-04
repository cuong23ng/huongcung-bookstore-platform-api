package com.huongcung.core.logistics.external.ghn.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDistrictResponse {
    @JsonProperty("DistrictID")
    private Integer districtId;
    
    @JsonProperty("DistrictName")
    private String districtName;
    
    @JsonProperty("ProvinceID")
    private Integer provinceId;

    @JsonProperty("SupportType")
    private Integer supportType;

    @JsonProperty("Status")
    private Integer status;
}

