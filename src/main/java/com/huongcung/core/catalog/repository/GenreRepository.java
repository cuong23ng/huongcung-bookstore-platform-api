package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.model.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {
    List<GenreEntity> findByIdIn(List<Long> ids);
}


