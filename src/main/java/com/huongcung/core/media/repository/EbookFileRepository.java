package com.huongcung.core.media.repository;

import com.huongcung.core.media.model.entity.EbookFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EbookFileRepository extends JpaRepository<EbookFileEntity, Long> {
}
