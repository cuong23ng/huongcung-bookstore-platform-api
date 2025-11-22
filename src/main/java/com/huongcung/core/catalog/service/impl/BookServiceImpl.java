package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.mapper.BookMapper;
import com.huongcung.core.catalog.model.dto.BookDTO;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("coreBookService")
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AbstractBookRepository abstractBookRepository;
    private final BookMapper bookMapper;

    @Override
    public List<BookDTO> findAll() {
        // Get all AbstractBookEntity and convert to BookDTO
        List<AbstractBookEntity> abstractBooks = abstractBookRepository.findAll();
        return abstractBooks.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO findBookByCode(String code) {
        AbstractBookEntity abstractBook = abstractBookRepository.findByCode(code);
        if (abstractBook != null) {
            return bookMapper.toDto(abstractBook);
        }
        return null;
    }

    @Override
    public List<BookDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        
        // Get from AbstractBookRepository
        List<AbstractBookEntity> abstractBooks = abstractBookRepository.findAllById(ids);
        return abstractBooks.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }
}
