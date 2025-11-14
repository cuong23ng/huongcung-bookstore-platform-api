package com.huongcung.core.order.repository;

import com.huongcung.core.order.model.entity.OrderEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEntryRepository extends JpaRepository<OrderEntryEntity, Long> {
}

