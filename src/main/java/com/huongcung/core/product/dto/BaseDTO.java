package com.huongcung.core.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BaseDTO implements Serializable {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
}
