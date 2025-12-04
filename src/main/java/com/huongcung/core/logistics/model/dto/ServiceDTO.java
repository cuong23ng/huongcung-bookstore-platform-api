package com.huongcung.core.logistics.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceDTO {
    private String serviceId;
    private String serviceTypeId;
    private String shortName;
}
