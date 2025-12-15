package com.huongcung.core.logistics.external.ghn.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnWardDTO {
    private String wardCode;
    private String wardName;
}
