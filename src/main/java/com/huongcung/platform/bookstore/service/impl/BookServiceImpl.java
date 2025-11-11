package com.huongcung.platform.bookstore.service.impl;

import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.product.service.AbstractBookService;
import com.huongcung.platform.bookstore.mapper.BookViewMapper;
import com.huongcung.platform.bookstore.model.BookData;
import com.huongcung.platform.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AbstractBookService abstractBookService;
    private final BookViewMapper bookViewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BookData> getAllBooks() {
        List<AbstractBookDTO> bookDTOs = abstractBookService.findAll();
        return bookDTOs.stream().map(bookViewMapper::toBookData).toList();
    }

    @Override
    public BookData getBookDetails(String code) {
        AbstractBookDTO bookDTO = abstractBookService.findBookByCode(code);
        return bookViewMapper.toBookData(bookDTO);
    }
}
