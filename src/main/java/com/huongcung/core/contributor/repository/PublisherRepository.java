package com.huongcung.core.contributor.repository;

import com.huongcung.core.contributor.model.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherEntity, Long> {
    org.springframework.data.domain.Page<PublisherEntity> findByNameContainingIgnoreCase(String name, org.springframework.data.domain.Pageable pageable);
}


