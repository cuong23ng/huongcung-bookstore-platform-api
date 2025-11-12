package com.huongcung.core.user.repository;

import com.huongcung.core.user.enumeration.StaffType;
import com.huongcung.core.user.model.entity.StaffEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {
    
    /**
     * Find all staff with pagination and optional filtering
     * @param staffType optional filter by staff type
     * @param assignedCity optional filter by assigned city
     * @param pageable pagination parameters
     * @return Page of StaffEntity
     */
    @Query("SELECT s FROM StaffEntity s WHERE " +
           "(:staffType IS NULL OR s.staffType = :staffType) AND " +
           "(:assignedCity IS NULL OR s.assignedCity = :assignedCity)")
    Page<StaffEntity> findAllWithFilters(
            @Param("staffType") StaffType staffType,
            @Param("assignedCity") String assignedCity,
            Pageable pageable
    );
}

