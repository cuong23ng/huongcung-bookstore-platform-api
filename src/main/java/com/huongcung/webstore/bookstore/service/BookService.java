package com.huongcung.webstore.bookstore.service;

import com.huongcung.webstore.bookstore.model.BookData;

import java.util.List;

public interface BookService {
    List<BookData> getAllBooks();

    BookData getBookDetails(String code);
}
