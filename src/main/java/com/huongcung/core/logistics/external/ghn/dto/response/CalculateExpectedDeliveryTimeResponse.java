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
public class CalculateExpectedDeliveryTimeResponse {
    @JsonProperty("leadtime")
    private String leadTime;

    @JsonProperty("order_date")
    private String orderDate;
}
