package com.huongcung.core.search.service.impl;

import com.huongcung.core.catalog.converter.AbstractBookConverter;
import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.catalog.service.AbstractBookService;
import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.model.dto.SearchFacet;
import com.huongcung.core.search.model.dto.SearchRequest;
import com.huongcung.core.search.model.dto.SearchResponse;
import com.huongcung.core.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseSearchServiceImpl implements SearchService {

    private final AbstractBookService abstractBookService;
    private final AbstractBookConverter abstractBookConverter;

    @Override
    public SearchResponse searchBooks(SearchRequest request) {
        log.info("Using database fallback search");

        long startTime = System.currentTimeMillis();

        // Get all books from database
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<AbstractBook> allBooks = abstractBookService.findAll(pageable);

        log.info("searchBooks: {} books", allBooks.getTotalElements());

        // Apply basic filtering in memory
        List<AbstractBook> filteredBooks = allBooks
                .stream()
                .filter(book -> matchesQuery(book, request.getQ()))
                .filter(book -> matchesGenres(book, request.getGenres()))
                .filter(book -> matchesLanguage(book, request.getLanguages()))
                .filter(book -> matchesFormat(book, request.getFormats()))
                .toList();

        log.info("searchBooks: {} filteredBooks", filteredBooks.size());

        List<BookFrontPageDTO> books = filteredBooks.stream().map(abstractBookConverter::convert).toList();

        // Build pagination
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .totalResults((long) filteredBooks.size())
                .totalPages((int) Math.ceil((double) filteredBooks.size() / request.getSize()))
                .hasNext(allBooks.hasNext())
                .hasPrevious(request.getPage() > 1)
                .build();

        long executionTime = System.currentTimeMillis() - startTime;

        return SearchResponse.builder()
                .books(books)
                .facets(Collections.emptyMap())
                .pagination(pagination)
                .highlightedFields(Collections.emptyMap())
                .executionTimeMs(executionTime)
                .fallbackUsed(true)
                .build();
    }

    @Override
    public List<String> getSuggestions(String query) {
        return List.of();
    }

    @Override
    public Map<String, List<SearchFacet>> getFacets(SearchRequest request) {
        return Map.of();
    }

    /**
     * Check if book matches query string (basic text matching)
     */
    private boolean matchesQuery(AbstractBook book, String query) {
        log.info("Check matchesQuery: {}", book.getTitle());
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery)) ||
                (book.getDescription() != null && book.getDescription().toLowerCase().contains(lowerQuery));
    }

    /**
     * Check if book matches genre filters
     */
    private boolean matchesGenres(AbstractBook book, List<String> genres) {
        log.info("Check matchesGenres: {}", book.getTitle());
        if (genres == null || genres.isEmpty()) {
            return true;
        }
        // Limitation: AbstractBookDTO doesn't have genres field
        // In fallback mode, genre filtering is not available
        // This is acceptable as fallback is a degraded mode
        log.debug("Genre filtering not available in fallback mode for book: {}", book.getCode());
        return true;
    }

    /**
     * Check if book matches language filters
     */
    private boolean matchesLanguage(AbstractBook book, List<String> languages) {
        log.info("Check matchesLanguage: {}", book.getTitle());
        if (languages == null || languages.isEmpty()) {
            return true;
        }
        return book.getLanguage() != null &&
                languages.contains(book.getLanguage().toString());
    }

    /**
     * Check if book matches format filters
     */
    private boolean matchesFormat(AbstractBook book, List<String> formats) {
        log.info("Check matchesFormat: {}", book.getTitle());
        if (formats == null || formats.isEmpty()) {
            return true;
        }
        for (String format : formats) {
            if ("PHYSICAL".equals(format) && book.hasPhysicalEdition()) {
                return true;
            }
            if ("DIGITAL".equals(format) && book.hasEbookEdition()) {
                return true;
            }
            if ("BOTH".equals(format) && book.hasPhysicalEdition() && book.hasEbookEdition()) {
                return true;
            }
        }
        return false;
    }
}
