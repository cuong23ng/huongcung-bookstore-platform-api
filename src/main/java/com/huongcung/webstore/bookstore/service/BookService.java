package com.huongcung.webstore.bookstore.service;

import com.huongcung.webstore.bookstore.model.BookData;
import com.huongcung.webstore.bookstore.model.BookFrontPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    List<BookData> getAllBooks();

    BookData getBookDetails(String code);

    Page<BookFrontPageDTO> getBooksForFrontPage(Pageable pageable);
}
