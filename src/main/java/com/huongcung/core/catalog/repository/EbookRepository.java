package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.model.entity.EbookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EbookRepository extends JpaRepository<EbookEntity, Long> {
}
