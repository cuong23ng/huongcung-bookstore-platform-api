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
public class GetOrderStatusResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("finish_date")
    private String finishDate;

    @JsonProperty("leadtime")
    private String leadTime;
}
