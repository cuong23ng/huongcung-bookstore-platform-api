package com.huongcung.core.logistics.repository;

import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsignmentEntityRepository extends JpaRepository<ConsignmentEntryEntity, Long> {
}
