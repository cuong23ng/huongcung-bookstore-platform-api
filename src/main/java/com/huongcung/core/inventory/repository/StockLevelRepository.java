package com.huongcung.core.inventory.repository;

import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends 
        JpaRepository<StockLevelEntity, Long>, 
        JpaSpecificationExecutor<StockLevelEntity> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevelEntity s WHERE s.book.abstractBook.id = :bookId AND s.warehouse.id = :warehouseId")
    Optional<StockLevelEntity> findByBookIdAndWarehouseIdWithLock(
        @Param("bookId") Long bookId,
        @Param("warehouseId") Long warehouseId
    );
    
    Optional<StockLevelEntity> findByBookAbstractBookIdAndWarehouseId(Long bookId, Long warehouseId);
}

