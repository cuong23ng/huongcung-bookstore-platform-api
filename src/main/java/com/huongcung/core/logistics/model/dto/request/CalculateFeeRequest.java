package com.huongcung.core.logistics.model.dto.request;

import com.huongcung.storefront.checkout.dto.CheckoutItemDTO;
import com.huongcung.storefront.checkout.dto.ShippingAddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFeeRequest {

    @NotEmpty(message = "Cart items are required")
    @Valid
    private List<CheckoutItemDTO> items;

    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDTO shippingAddress;
}

