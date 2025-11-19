package com.huongcung.webstore.bookstore.service.impl;

import com.huongcung.core.contributor.mapper.AuthorMapper;
import com.huongcung.core.contributor.model.dto.AuthorDTO;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.service.AbstractBookService;
import com.huongcung.core.storage.service.StorageService;
import com.huongcung.webstore.bookstore.converter.AuthorConverter;
import com.huongcung.webstore.bookstore.converter.BookFrontPageConverter;
import com.huongcung.webstore.bookstore.repository.WebBookRepository;
import com.huongcung.webstore.bookstore.mapper.BookViewMapper;
import com.huongcung.webstore.bookstore.model.BookData;
import com.huongcung.webstore.bookstore.model.BookFrontPageDTO;
import com.huongcung.webstore.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final AbstractBookService abstractBookService;
    private final BookViewMapper bookViewMapper;
    private final AuthorMapper authorMapper;
    private final WebBookRepository webBookRepository;
    private final BookFrontPageConverter bookFrontPageConverter;
    private final AuthorConverter authorConverter;
    private final StorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public List<BookData> getAllBooks() {
        List<AbstractBookDTO> bookDTOs = abstractBookService.findAll();
        return bookDTOs.stream().map(bookViewMapper::toBookData).toList();
    }

    @Override
    public BookData getBookDetails(String code) {
        AbstractBookDTO bookDTO = abstractBookService.findBookByCode(code);
        return bookViewMapper.toBookData(bookDTO);
    }

    private Map<String, List<AuthorDTO>> findAllAuthorsByBookCodes(List<String> bookCodes) {
        Map<String, List<AuthorEntity>> authorsBookMap = webBookRepository.findAllAuthorsByBookCodesIn(bookCodes);
        
        return authorsBookMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> authorMapper.toDto(entry.getValue())
                ));
    }

    @Transactional(readOnly = true)
    public Page<BookFrontPageDTO> getBooksForFrontPage(Pageable pageable) {

        Page<BookFrontPageDTO> bookDataPage = webBookRepository.findFrontPageBookList(pageable);

        bookDataPage.stream().parallel().forEach(b -> {
            String imgRelativePath = b.getThumbnailUrl();
            b.setThumbnailUrl(storageService.getFullUrl(imgRelativePath));
        });

        List<String> bookCodes = bookDataPage.stream().map(BookFrontPageDTO::getCode).toList();
        Map<String, List<AuthorDTO>> authorsBookMap = findAllAuthorsByBookCodes(bookCodes);

        bookDataPage.stream().parallel().forEach(bookData -> {
            List<AuthorDTO> authors = authorsBookMap.get(bookData.getCode());
            bookData.setAuthors(authors.parallelStream().map(authorConverter::convert).toList());
        });

        return bookDataPage;
    }
}
