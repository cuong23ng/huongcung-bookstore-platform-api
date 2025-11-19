package com.huongcung.webstore.bookstore.repository;

import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.catalog.model.entity.BookEntity;
import com.huongcung.webstore.bookstore.model.BookFrontPageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface WebBookRepository extends JpaRepository<BookEntity, Long> {

    @Query("SELECT new com.huongcung.webstore.bookstore.model.BookFrontPageDTO(" +
            "b.code, " +
            "b.title, " +
            "COALESCE((SELECT bi.url FROM BookImageEntity bi JOIN bi.books bk WHERE bk.id = b.id AND bi.position = 1), ''), " +
            "pb.currentPrice, " +
            "eb.currentPrice" +
            ") " +
            "FROM BookEntity b " +
            "LEFT JOIN PhysicalBookEntity pb ON pb.id = b.id " +
            "LEFT JOIN EbookEntity eb ON eb.id = b.id " +
            "WHERE b.isActive = true " +
            "ORDER BY b.code")
    Page<BookFrontPageDTO> findFrontPageBookList(Pageable pageable);

    @Query("SELECT b.code, a " +
            "FROM BookEntity b " +
            "JOIN b.authors a " +
            "WHERE b.code IN :bookCodes " +
            "ORDER BY b.code, a.name")
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
