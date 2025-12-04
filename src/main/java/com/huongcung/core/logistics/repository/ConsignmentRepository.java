package com.huongcung.core.logistics.repository;

import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsignmentRepository extends JpaRepository<ConsignmentEntity, Long> {
    Optional<ConsignmentEntity> findByTrackingNumber(String trackingNumber);
}
