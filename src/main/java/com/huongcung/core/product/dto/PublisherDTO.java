package com.huongcung.core.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Locale;
import java.util.spi.LocaleNameProvider;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PublisherDTO extends BaseDTO {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
}
