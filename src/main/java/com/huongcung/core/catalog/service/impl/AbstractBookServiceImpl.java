package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.converter.AbstractBookConverter;
import com.huongcung.core.catalog.converter.BookDetailsConverter;
import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.model.dto.BookDTO;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.service.AbstractBookService;
import com.huongcung.core.catalog.mapper.AbstractBookMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public AbstractBook findByCode(String code) {
        return abstractBookMapper.toDomain(abstractBookRepository.findByCode(code));
    }

    private List<AbstractBook> findByIds(List<Long> ids) {
        return abstractBookMapper.toDomain(abstractBookRepository.findAllById(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookFrontPageDTO> getBooksForFrontPage(Pageable pageable) {
        Page<AbstractBook> books = findAll(pageable);
        return books.stream().parallel().map(abstractBookConverter::convert).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractBookDTO getBookDetails(String code) {
        AbstractBook book = findByCode(code);
        return bookDetailsConverter.convert(book);
    }

    @Override
    public List<BookFrontPageDTO> getBooksForFrontPageByIds(List<Long> ids) {
        List<AbstractBook> abstractBooks = findByIds(ids);
        return abstractBooks.stream()
                .map(abstractBookConverter::convert)
                .toList();
    }
}
