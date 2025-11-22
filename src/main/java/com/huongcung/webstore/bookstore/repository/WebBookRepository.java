package com.huongcung.webstore.bookstore.repository;

import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.webstore.bookstore.model.BookFrontPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface WebBookRepository extends JpaRepository<AbstractBookEntity, Long> {

    @Query("SELECT new com.huongcung.webstore.bookstore.model.BookFrontPageDTO(" +
            "ab.code, " +
            "ab.title, " +
            "COALESCE((SELECT bi.url FROM BookImageEntity bi WHERE bi.book.id = ab.id AND bi.position = 1), ''), " +
            "pb.currentPrice, " +
            "eb.currentPrice" +
            ") " +
            "FROM AbstractBookEntity ab " +
            "LEFT JOIN ab.physicalBookInfo pb " +
            "LEFT JOIN ab.ebookInfo eb " +
            "WHERE (pb IS NULL OR pb.isAvailable = true) AND (eb IS NULL OR eb.isActive = true) " +
            "ORDER BY ab.code")
    Page<BookFrontPageDTO> findFrontPageBookList(Pageable pageable);

    @Query("SELECT ab.code, a " +
            "FROM AbstractBookEntity ab " +
            "JOIN ab.authors a " +
            "WHERE ab.code IN :bookCodes " +
            "ORDER BY ab.code, a.name")
    List<Object[]> findAllAuthorsByBookCodesInRaw(List<String> bookCodes);
    
    default Map<String, List<AuthorEntity>> findAllAuthorsByBookCodesIn(List<String> bookCodes) {
        return findAllAuthorsByBookCodesInRaw(bookCodes).stream()
                .collect(Collectors.groupingBy(
                        row -> (String) row[0],
                        Collectors.mapping(
                                row -> (AuthorEntity) row[1],
                                Collectors.toList()
                        )
                ));
    }
}
