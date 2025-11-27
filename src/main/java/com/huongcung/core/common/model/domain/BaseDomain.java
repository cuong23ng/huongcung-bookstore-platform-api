package com.huongcung.core.common.model.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseDomain {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
