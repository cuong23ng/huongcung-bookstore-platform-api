package com.huongcung.storefront.checkout.dto;

import com.huongcung.core.order.enumeration.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    // Information for GUEST
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "Phone is required")
    private String phone;
}

