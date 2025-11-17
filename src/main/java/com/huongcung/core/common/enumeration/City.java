package com.huongcung.core.common.enumeration;

public enum City {
    HANOI("Ha Noi"),
    HCMC("Ho Chi Minh"),
    DANANG("Da Nang");

    public final String name;
    City (String name) {
        this.name = name;
    }
}
