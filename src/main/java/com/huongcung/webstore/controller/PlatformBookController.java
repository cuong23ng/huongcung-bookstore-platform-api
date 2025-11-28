package com.huongcung.webstore.controller;

import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.model.dto.response.GetBookFrontPageResponse;
import com.huongcung.core.catalog.service.AbstractBookService;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlatformBookController {

    private final AbstractBookService abstractBookService;

    /**
     * Get all books
     * @return list of books
     */
    @GetMapping("")
    public ResponseEntity<BaseResponse> getAllBooks(Pageable pageable) {
        GetBookFrontPageResponse bookFrontPageResponse = abstractBookService.getBooksForFrontPage(pageable);
        return ResponseEntity.ok(BaseResponse.builder().data(bookFrontPageResponse).build());
    }

    /**
     * Get book details
     * @return book
     */
    @GetMapping("/{code}")
    public ResponseEntity<BaseResponse> getBookDetails(@PathVariable String code) {
        AbstractBookDTO book = abstractBookService.getBookDetails(code);
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
