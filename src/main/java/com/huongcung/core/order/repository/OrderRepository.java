package com.huongcung.core.order.repository;

import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    
    Page<OrderEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    List<OrderEntity> findAllByStatus(OrderStatus status);
    
    @Query("SELECT o FROM OrderEntity o WHERE " +
           "(:status IS NULL OR o.status = :status) " +
           "ORDER BY o.createdAt DESC")
    Page<OrderEntity> findAllWithFilters(@Param("status") OrderStatus status, Pageable pageable);
}

