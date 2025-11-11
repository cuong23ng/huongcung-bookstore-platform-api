package com.huongcung.core.controller;

import com.huongcung.core.product.model.dto.AbstractBookDTO;
import com.huongcung.core.common.model.response.BaseResponse;
import com.huongcung.core.product.service.AbstractBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/core/books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookController {

    private final AbstractBookService abstractBookService;

    /**
     * Get all books
     * @return list of books
     */
    @GetMapping("")
    public ResponseEntity<BaseResponse> getAllBooks() {
        List<AbstractBookDTO> books = abstractBookService.findAll();
        return ResponseEntity.ok(BaseResponse.builder().data(books).build());
    }

    /**
     * Health check endpoint for authentication service
     * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "Book service is running"));
    }
}
