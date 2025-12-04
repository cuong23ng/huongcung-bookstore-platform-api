package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnServiceDTO {
    private Integer serviceId;
    private Integer serviceTypeId;
    private String shortName;
}
