package com.huongcung.core.common.mapper;

import java.util.List;

public interface DomainMapper <D, E> {
    //E toEntity(D dto);
    E toDomain(D entity);
    //List<E> toEntity(List<D> dtoList);
    List<E> toDomain(List<D> entityList);
}
