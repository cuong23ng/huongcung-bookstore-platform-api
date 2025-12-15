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
public class GetServiceRequest {
    @JsonProperty("from_district")
    private Integer fromDistrict;

    @JsonProperty("to_district")
    private Integer toDistrict;
}
