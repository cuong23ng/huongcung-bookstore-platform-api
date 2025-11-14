package com.huongcung.core.order.repository;

import com.huongcung.core.order.model.entity.DeliveryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfoEntity, Long> {
    Optional<DeliveryInfoEntity> findByOrderId(Long orderId);
}

