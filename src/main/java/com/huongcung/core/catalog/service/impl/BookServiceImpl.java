package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.mapper.BookMapper;
import com.huongcung.core.catalog.model.dto.BookDTO;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.BookEntity;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        List<BookDTO> bookDTOs = new ArrayList<>();
        
        for (AbstractBookEntity abstractBook : abstractBooks) {
            // Get the related BookEntity (PhysicalBookEntity or EbookEntity)
            BookEntity bookEntity = getRelatedBookEntity(abstractBook);
            if (bookEntity != null) {
                bookDTOs.add(bookMapper.toDto(bookEntity));
            }
        }
        
        return bookDTOs;
    }

    @Override
    public BookDTO findBookByCode(String code) {
        AbstractBookEntity abstractBook = abstractBookRepository.findByCode(code);
        if (abstractBook != null) {
            BookEntity bookEntity = getRelatedBookEntity(abstractBook);
            if (bookEntity != null) {
                return bookMapper.toDto(bookEntity);
            }
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
        List<BookDTO> bookDTOs = new ArrayList<>();
        
        for (AbstractBookEntity abstractBook : abstractBooks) {
            BookEntity bookEntity = getRelatedBookEntity(abstractBook);
            if (bookEntity != null) {
                bookDTOs.add(bookMapper.toDto(bookEntity));
            }
        }
        
        return bookDTOs;
    }
    
    /**
     * Get the related BookEntity (PhysicalBookEntity or EbookEntity) from AbstractBookEntity
     */
    private BookEntity getRelatedBookEntity(AbstractBookEntity abstractBook) {
        if (abstractBook.getPhysicalBookInfo() != null) {
            return abstractBook.getPhysicalBookInfo();
        } else if (abstractBook.getEbookInfo() != null) {
            return abstractBook.getEbookInfo();
        }
        return null;
    }

}
