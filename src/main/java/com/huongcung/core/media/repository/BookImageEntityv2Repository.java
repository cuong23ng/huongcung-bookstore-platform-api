package com.huongcung.core.media.repository;

import com.huongcung.core.media.model.entity.BookImageEntityv2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageEntityv2Repository extends JpaRepository<BookImageEntityv2, Long> {
    @Query("SELECT bi FROM BookImageEntityv2 bi WHERE bi.book.id = :bookId ORDER BY bi.position ASC")
    List<BookImageEntityv2> findByBookId(@Param("bookId") Long bookId);
}

