package com.huongcung.core.order.repository;

import com.huongcung.core.order.model.entity.OrderCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCustomerRepository extends JpaRepository<OrderCustomerEntity, Long> {
}
