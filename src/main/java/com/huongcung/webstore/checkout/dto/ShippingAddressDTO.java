package com.huongcung.webstore.checkout.dto;

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
    private Integer provinceId;
    
    @NotNull(message = "District ID is required")
    private Integer districtId;
    
    @NotBlank(message = "Ward code is required")
    private String wardCode;
    
    private String postalCode;
}

