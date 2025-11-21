package com.huongcung.core.catalog.service;

import java.util.List;

import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AbstractBookService {
  Page<AbstractBook> findAll(Pageable pageable);
  AbstractBook findByCode(String code);
  List<BookFrontPageDTO> getBooksForFrontPage(Pageable pageable);
}
