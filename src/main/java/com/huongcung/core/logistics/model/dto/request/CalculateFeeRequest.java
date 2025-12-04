package com.huongcung.core.logistics.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFeeRequest {
    @NotNull(message = "District ID is required")
    private String districtId;
    
    @NotNull(message = "Ward code is required")
    private String wardCode;
    
    @NotNull(message = "Weight is required")
    private Integer weight; // in grams
    
    private String serviceTypeId; // Optional, defaults to standard
}

