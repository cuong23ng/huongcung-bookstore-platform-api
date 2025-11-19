package com.huongcung.core.catalog.service;

import com.huongcung.core.catalog.model.dto.AbstractBookDTO;

import java.util.List;

public interface AbstractBookService {
    List<AbstractBookDTO> findAll();

    AbstractBookDTO findBookByCode(String code);
    
    List<AbstractBookDTO> findByIds(List<Long> ids);
}
