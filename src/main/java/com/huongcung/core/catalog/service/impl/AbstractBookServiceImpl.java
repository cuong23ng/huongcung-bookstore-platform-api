package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.converter.AbstractBookConverter;
import com.huongcung.core.catalog.model.domain.AbstractBook;
import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class AbstractBookServiceImpl implements AbstractBookService {
    private final AbstractBookRepository abstractBookRepository;
    private final AbstractBookMapper abstractBookMapper;
    private final AbstractBookConverter abstractBookConverter;

    @Override
    public Page<AbstractBook> findAll(Pageable pageable) {
        return abstractBookRepository.findAll(pageable).map(abstractBookMapper::toDomain);
    }

    @Override
    public AbstractBook findByCode(String code) {
        return abstractBookMapper.toDomain(abstractBookRepository.findByCode(code));
    }

    @Transactional(readOnly = true)
    public List<BookFrontPageDTO> getBooksForFrontPage(Pageable pageable) {

        Page<AbstractBook> books = findAll(pageable);

        return books.stream().parallel().map(abstractBookConverter::convert).toList();
    }
}
