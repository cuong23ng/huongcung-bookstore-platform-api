package com.huongcung.core.logistics.external.ghn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookDTO {
    @JsonProperty("OrderCode")
    private String orderCode;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Time")
    private Long time;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("CODAmount")
    private Double codAmount;
}