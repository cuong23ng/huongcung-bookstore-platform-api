package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.model.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
    BookEntity findBookEntityByCode(String code);
    
    List<BookEntity> findByIdIn(List<Long> ids);
    
    List<BookEntity> findByCodeIn(List<String> codes);
}
