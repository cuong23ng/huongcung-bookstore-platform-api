package com.huongcung.core.catalog.service;

import com.huongcung.core.catalog.model.dto.BookDTO;

import java.util.List;

public interface BookService {
    List<BookDTO> findAll();

    BookDTO findBookByCode(String code);
    
    List<BookDTO> findByIds(List<Long> ids);
}
