package com.huongcung.core.user.repository;

import com.huongcung.core.user.model.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    
    /**
     * Find customer by email address
     * @param email the email address
     * @return Optional containing the customer if found
     */
    Optional<CustomerEntity> findByEmail(String email);
    
    /**
     * Find customer by UID
     * @param uid the unique identifier
     * @return Optional containing the customer if found
     */
    Optional<CustomerEntity> findByUid(String uid);


}

