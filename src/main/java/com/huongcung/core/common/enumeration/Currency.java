package com.huongcung.core.common.enumeration;

public enum Currency {
    VND("VND"),
    USD("$");

    public final String symbol;
    Currency (String symbol) {
        this.symbol = symbol;
    }
}
