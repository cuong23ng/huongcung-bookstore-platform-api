package com.huongcung.core.product.repository;

import com.huongcung.core.product.entity.AbstractBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractBookRepository extends JpaRepository<AbstractBookEntity, Long> {

}
