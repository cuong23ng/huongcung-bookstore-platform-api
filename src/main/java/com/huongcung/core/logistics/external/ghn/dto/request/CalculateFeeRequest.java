package com.huongcung.core.logistics.external.ghn.dto.request;

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

    @JsonProperty("insurance_value")
    private Integer insuranceValue;

    @JsonProperty("coupon")
    private Integer coupon;

    @JsonProperty("cod_failed_amount")
    private Integer codFailedAmount;

    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;
    
    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    
    @JsonProperty("weight")
    private Integer weight; // in grams
    
    @JsonProperty("length")
    private Integer length; // in cm
    
    @JsonProperty("width")
    private Integer width; // in cm
    
    @JsonProperty("height")
    private Integer height; // in cm

    @JsonProperty("cod_value")
    private Integer codValue;
}

