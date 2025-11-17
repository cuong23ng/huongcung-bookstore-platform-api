package com.huongcung.core.inventory.repository;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
    List<WarehouseEntity> findByCity(City city);
}

