package com.huongcung.core.product.service;

import com.huongcung.core.product.model.dto.AbstractBookDTO;

import java.util.List;

public interface AbstractBookService {
    List<AbstractBookDTO> findAll();

    AbstractBookDTO findBookByCode(String code);
    
    List<AbstractBookDTO> findByIds(List<Long> ids);
}
