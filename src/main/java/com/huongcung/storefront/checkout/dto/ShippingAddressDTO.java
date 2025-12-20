package com.huongcung.storefront.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotNull(message = "Province ID is required")
    private String provinceId;
    
    private String provinceName;
    
    @NotNull(message = "District ID is required")
    private String districtId;
    
    private String districtName;
    
    @NotBlank(message = "Ward code is required")
    private String wardCode;
    
    private String wardName;

    @NotBlank(message = "Service Type id is required")
    private String serviceTypeId;
    
    private String postalCode;
}

