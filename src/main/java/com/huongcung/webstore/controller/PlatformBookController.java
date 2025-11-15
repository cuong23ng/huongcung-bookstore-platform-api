package com.huongcung.webstore.controller;

import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.webstore.bookstore.model.BookData;
import com.huongcung.webstore.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlatformBookController {

    private final BookService bookService;

    /**
     * Get all books
     * @return list of books
     */
    @GetMapping("")
    public ResponseEntity<BaseResponse> getAllBooks() {
        List<BookData> books = bookService.getAllBooks();
        return ResponseEntity.ok(BaseResponse.builder().data(books).build());
    }

    /**
     * Get book details
     * @return book
     */
    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse> getBookDetails(@PathVariable String code) {
       BookData book = bookService.getBookDetails(code);
        return ResponseEntity.ok(BaseResponse.builder().data(book).build());
    }

    /**
     * Health check endpoint for authentication service
     * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Platform Book service is running"));
    }
}
