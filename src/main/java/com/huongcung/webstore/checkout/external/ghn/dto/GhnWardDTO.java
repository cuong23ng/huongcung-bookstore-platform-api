package com.huongcung.webstore.checkout.external.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GhnWardDTO {
    @JsonProperty("WardCode")
    private String wardCode;
    
    @JsonProperty("WardName")
    private String wardName;
    
    @JsonProperty("DistrictID")
    private Integer districtId;
}

