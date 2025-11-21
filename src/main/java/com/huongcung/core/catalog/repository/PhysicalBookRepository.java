package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhysicalBookRepository extends JpaRepository<PhysicalBookEntity, Long> {
}
