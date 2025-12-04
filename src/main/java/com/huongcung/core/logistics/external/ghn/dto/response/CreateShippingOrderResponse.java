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
public class CreateShippingOrderResponse {
    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime;

    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("sort_code")
    private String sortCode;

    @JsonProperty("total_fee")
    private Integer totalFee;

    @JsonProperty("trans_type")
    private String transType;
}
