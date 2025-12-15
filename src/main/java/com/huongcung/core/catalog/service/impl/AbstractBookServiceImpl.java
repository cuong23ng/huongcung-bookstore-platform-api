package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.converter.AbstractBookConverter;
import com.huongcung.core.catalog.converter.BookDetailsConverter;
import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.model.dto.BookDTO;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.catalog.model.dto.response.GetBookFrontPageResponse;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.service.AbstractBookService;
import com.huongcung.core.catalog.mapper.AbstractBookMapper;

import com.huongcung.core.search.model.dto.PaginationInfo;
import com.huongcung.core.search.model.dto.SearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AbstractBookServiceImpl implements AbstractBookService {
    private final AbstractBookRepository abstractBookRepository;
    private final AbstractBookMapper abstractBookMapper;
    private final AbstractBookConverter abstractBookConverter;
    private final BookDetailsConverter bookDetailsConverter;

    @Override
    public Page<AbstractBook> findAll(Pageable pageable) {
        return abstractBookRepository.findAll(pageable).map(abstractBookMapper::toDomain);
    }

    @Override
    public AbstractBook findById(Long id) {
        return abstractBookMapper.toDomain(abstractBookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + id)));
    }

    @Override
    public AbstractBook findByCode(String code) {
        return abstractBookMapper.toDomain(abstractBookRepository.findByCode(code));
    }

    private List<AbstractBook> findByIds(List<Long> ids) {
        return abstractBookMapper.toDomain(abstractBookRepository.findAllById(ids));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "frontPage", key = "'page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    public GetBookFrontPageResponse getBooksForFrontPage(Pageable pageable) {
        Page<AbstractBook> books = findAll(pageable);
        PaginationInfo pagination = PaginationInfo.builder()
                .currentPage(books.getNumber())
                .pageSize(books.getSize())
                .totalResults(books.getTotalElements())
                .totalPages(books.getTotalPages())
                .hasNext(books.hasNext())
                .hasPrevious(books.hasPrevious())
                .build();
        return GetBookFrontPageResponse.builder()
                .books(books.map(abstractBookConverter::convert).toList())
                .pagination(pagination)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = "bookDetails", key = "'bookCode:' + #code")
    public AbstractBookDTO getBookDetails(String code) {
        AbstractBook book = findByCode(code);
        return bookDetailsConverter.convert(book);
    }

    @Override
    public List<BookFrontPageDTO> getBooksForFrontPageByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<AbstractBook> abstractBooks = findByIds(ids);

        Map<Long, AbstractBook> bookMap = abstractBooks.stream()
            .collect(Collectors.toMap(
                AbstractBook::getId,
                book -> book,
                (existing, replacement) -> existing
            ));


        return ids.stream()
            .map(bookMap::get)
            .filter(Objects::nonNull)
            .map(abstractBookConverter::convert)
            .toList();
    }
}
