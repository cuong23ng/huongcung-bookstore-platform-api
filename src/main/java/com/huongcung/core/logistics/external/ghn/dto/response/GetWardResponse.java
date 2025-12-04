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
public class GetWardResponse {
    @JsonProperty("WardCode")
    private String wardCode;
    
    @JsonProperty("WardName")
    private String wardName;
    
    @JsonProperty("DistrictID")
    private Integer districtId;

    @JsonProperty("SupportType")
    private Integer supportType;

    @JsonProperty("Status")
    private Integer status;
}

