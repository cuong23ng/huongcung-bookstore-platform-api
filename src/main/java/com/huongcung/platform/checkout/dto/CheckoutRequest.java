package com.huongcung.platform.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    @NotEmpty(message = "Cart items are required")
    @Valid
    private List<CheckoutItemDTO> items;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDTO shippingAddress;
    
    private String shippingMethod; // standard or express
}

