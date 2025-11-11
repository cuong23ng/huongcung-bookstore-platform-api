package com.huongcung.platform.bookstore.service;

import com.huongcung.platform.bookstore.model.BookData;

import java.util.List;

public interface BookService {
    List<BookData> getAllBooks();

    BookData getBookDetails(String code);
}
