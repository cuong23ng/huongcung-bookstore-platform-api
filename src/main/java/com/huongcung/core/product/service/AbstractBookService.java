package com.huongcung.core.product.service;

import com.huongcung.core.product.dto.AbstractBookDTO;

import java.util.List;

public interface AbstractBookService {
    List<AbstractBookDTO> findAll();
}
