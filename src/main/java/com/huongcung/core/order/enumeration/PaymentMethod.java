package com.huongcung.core.order.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentMethod {
    COD("Cash On Delivery"),
    VNPAY("VNPAY");

    private final String name;
}