package com.huongcung.core.contributor.repository;

import com.huongcung.core.contributor.model.entity.TranslatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranslatorRepository extends JpaRepository<TranslatorEntity, Long> {
    List<TranslatorEntity> findByIdIn(List<Long> ids);
    org.springframework.data.domain.Page<TranslatorEntity> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}


