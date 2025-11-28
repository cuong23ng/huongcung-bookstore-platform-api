package com.huongcung.webstore.checkout.external.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFeeResponse {
    @JsonProperty("total")
    private BigDecimal total;
    
    @JsonProperty("service_fee")
    private BigDecimal serviceFee;
    
    @JsonProperty("insurance_fee")
    private BigDecimal insuranceFee;
    
    @JsonProperty("pick_station_fee")
    private BigDecimal pickStationFee;
    
    @JsonProperty("coupon_value")
    private BigDecimal couponValue;
    
    @JsonProperty("r2s_fee")
    private BigDecimal r2sFee;
    
    @JsonProperty("return_again")
    private BigDecimal returnAgain;
}

