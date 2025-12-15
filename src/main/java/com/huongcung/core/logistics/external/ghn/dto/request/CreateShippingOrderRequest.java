package com.huongcung.core.logistics.external.ghn.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingOrderRequest {
    @JsonProperty("to_name")
    private String toName;

    @JsonProperty("from_name")
    private String fromName;

    @JsonProperty("from_phone")
    private String fromPhone;

    @JsonProperty("from_address")
    private String fromAddress;

    @JsonProperty("from_ward_name")
    private String fromWardName;

    @JsonProperty("from_district_name")
    private String fromDistrictName;

    @JsonProperty("from_provice_name")
    private String fromProvinceName;

    @JsonProperty("to_phone")
    private String toPhone;

    @JsonProperty("to_address")
    private String toAddress;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("client_order_code")
    private String clientOrderCode;

    @JsonProperty("cod_amount")
    private Integer codAmount;

    @JsonProperty("content")
    private String content;

    @JsonProperty("weight")
    private Integer weight;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("insurance_value")
    private Integer insuranceValue;

    @JsonProperty("coupon")
    private String coupon;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId; // 2: E-commerce Delivery, 5: Traditional Delivery

    @JsonProperty("payment_type_id")
    private Integer paymentTypeId; // 1: Shop/Seller; 2: Buyer/Consignee.

    @JsonProperty("note")
    private String note;

    @JsonProperty("required_note")
    private String requiredNote; // CHOTHUHANG, CHOXEMHANGKHONGTHU, KHONGCHOXEMHANG

    @JsonProperty("Items")
    private List<ShippingItemRequest> items;
}
