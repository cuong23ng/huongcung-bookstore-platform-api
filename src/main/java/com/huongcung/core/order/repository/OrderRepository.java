package com.huongcung.core.order.repository;

import com.huongcung.core.order.model.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    Page<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
}

