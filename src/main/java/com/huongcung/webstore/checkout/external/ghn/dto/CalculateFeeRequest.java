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
public class CalculateFeeRequest {
    @JsonProperty("service_id")
    private Integer serviceId;
    
    @JsonProperty("service_type_id")
    private Integer serviceTypeId;
    
    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    
    @JsonProperty("to_ward_code")
    private String toWardCode;
    
    @JsonProperty("weight")
    private Integer weight; // in grams
    
    @JsonProperty("length")
    private Integer length; // in cm
    
    @JsonProperty("width")
    private Integer width; // in cm
    
    @JsonProperty("height")
    private Integer height; // in cm
}

