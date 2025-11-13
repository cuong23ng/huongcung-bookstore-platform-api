package com.huongcung.core.media.repository;

import com.huongcung.core.media.model.entity.BookImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookImageRepository extends JpaRepository<BookImageEntity, Long> {
    List<BookImageEntity> findByBookId(Long bookId);
}


