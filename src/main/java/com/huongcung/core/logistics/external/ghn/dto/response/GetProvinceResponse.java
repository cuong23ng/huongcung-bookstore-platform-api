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
public class GetProvinceResponse {
    @JsonProperty("ProvinceID")
    private Integer provinceId;
    
    @JsonProperty("ProvinceName")
    private String provinceName;

    @JsonProperty("Status")
    private Integer status; // 1:Unlock; 2:Lock
}

