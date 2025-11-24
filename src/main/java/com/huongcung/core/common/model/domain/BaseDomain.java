package com.huongcung.core.common.model.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class BaseDomain {
    private Long id;
    private Date createdAt;
    private Date updatedAt;
}
