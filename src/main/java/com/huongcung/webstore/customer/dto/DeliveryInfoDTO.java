package com.huongcung.webstore.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoDTO {
    private Integer provinceId;
    private Integer districtId;
    private String wardCode;
    private Integer serviceTypeId;
    private Integer serviceId;
    private String expectedDeliveryTime;
    private String ghnOrderCode;
    private Integer weight; // in grams
    private Integer length; // in cm
    private Integer width; // in cm
    private Integer height; // in cm
}

