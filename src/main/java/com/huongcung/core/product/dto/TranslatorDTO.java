package com.huongcung.core.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class TranslatorDTO extends BaseDTO {
    private String name;
    private String biography;
    private String photoUrl;
    private Date birthDate;
}
