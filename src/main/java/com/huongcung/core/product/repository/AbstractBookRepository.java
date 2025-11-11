package com.huongcung.core.product.repository;

import com.huongcung.core.product.model.entity.AbstractBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbstractBookRepository extends JpaRepository<AbstractBookEntity, Long> {
    AbstractBookEntity findAbstractBookEntityByCode(String code);
    
    List<AbstractBookEntity> findByIdIn(List<Long> ids);
}
