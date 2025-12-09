package com.huongcung.core.logistics.repository;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsignmentRepository extends JpaRepository<ConsignmentEntity, Long> {

    Optional<ConsignmentEntity> findByTrackingNumber(String trackingNumber);

    // Tìm các đơn đang giao, sắp xếp theo thời gian check cũ nhất
    // Pageable dùng để giới hạn số lượng (ví dụ lấy 50 đơn đầu tiên)
    @Query("SELECT c FROM ConsignmentEntity c WHERE c.status IN :statuses ORDER BY c.updatedAt ASC NULLS FIRST")
    List<ConsignmentEntity> findOrdersToSync(@Param("statuses") List<ConsignmentStatus> statuses, Pageable pageable);
}
