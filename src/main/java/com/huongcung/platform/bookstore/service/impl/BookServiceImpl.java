package com.huongcung.platform.bookstore.service.impl;

import com.huongcung.core.product.dto.AbstractBookDTO;
import com.huongcung.core.product.service.AbstractBookService;
import com.huongcung.platform.bookstore.model.BookData;
import com.huongcung.platform.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AbstractBookService abstractBookService;

    @Override
    public List<BookData> getAllBooks() {
        List<AbstractBookDTO> bookDTOs = abstractBookService.findAll();
        return bookDTOs
                .stream()
                .map(bookDTO -> {
                    BookData bookResp = new BookData();
                    populate(bookDTO, bookResp);
                    return bookResp;
                }).toList();
    }

    private void populate(AbstractBookDTO source, BookData target) {

    }
}
