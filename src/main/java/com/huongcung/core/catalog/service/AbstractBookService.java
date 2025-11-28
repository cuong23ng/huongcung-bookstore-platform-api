package com.huongcung.core.catalog.service;

import java.util.List;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.catalog.model.dto.response.GetBookFrontPageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AbstractBookService {
    Page<AbstractBook> findAll(Pageable pageable);
    AbstractBook findById(Long id);
    AbstractBook findByCode(String code);
    GetBookFrontPageResponse getBooksForFrontPage(Pageable pageable);
    AbstractBookDTO getBookDetails(String code);
    List<BookFrontPageDTO> getBooksForFrontPageByIds(List<Long> ids);
}
