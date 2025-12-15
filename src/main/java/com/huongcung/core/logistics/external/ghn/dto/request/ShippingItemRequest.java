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
public class ShippingItemRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("weight")
    private Integer weight;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;
}
