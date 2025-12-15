package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnDistrictDTO {
    private Integer districtId;
    private String districtName;
}
