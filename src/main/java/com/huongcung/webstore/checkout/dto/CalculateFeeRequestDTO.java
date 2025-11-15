package com.huongcung.webstore.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFeeRequestDTO {
    @NotNull(message = "District ID is required")
    private Integer districtId;
    
    @NotNull(message = "Ward code is required")
    private String wardCode;
    
    @NotNull(message = "Weight is required")
    private Integer weight; // in grams
    
    private Integer serviceTypeId; // Optional, defaults to standard
}

