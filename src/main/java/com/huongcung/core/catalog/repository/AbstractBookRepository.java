package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractBookRepository extends JpaRepository<AbstractBookEntity, Long> {
    AbstractBookEntity findByCode(String code);
}
