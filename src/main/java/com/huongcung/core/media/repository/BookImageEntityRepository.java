package com.huongcung.core.media.repository;

import com.huongcung.core.media.model.entity.BookImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageEntityRepository extends JpaRepository<BookImageEntity, Long> {
    @Query("SELECT bi FROM BookImageEntity bi WHERE bi.book.id = :bookId ORDER BY bi.position ASC")
    List<BookImageEntity> findByBookId(@Param("bookId") Long bookId);
}

