package com.huongcung.core.inventory.repository;

import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevelEntity, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevelEntity s WHERE s.book = :book AND s.warehouse.city = :city")
    Optional<StockLevelEntity> findByBookAndCityWithLock(
        @Param("book") PhysicalBookEntity book,
        @Param("city") City city
    );
    
    Optional<StockLevelEntity> findByBookIdAndWarehouseCity(Long bookId, City city);
}

