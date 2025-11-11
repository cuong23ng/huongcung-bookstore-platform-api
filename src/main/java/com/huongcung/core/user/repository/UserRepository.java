package com.huongcung.core.user.repository;

import com.huongcung.core.user.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * Find user by email address
     * @param email the email address
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email
     * @param email the email address
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by UID
     * @param uid the unique identifier
     * @return Optional containing the user if found
     */
    Optional<UserEntity> findByUid(String uid);
    
    /**
     * Check if a user exists with the given UID
     * @param uid the unique identifier
     * @return true if user exists, false otherwise
     */
    boolean existsByUid(String uid);
    
    /**
     * Find user by email and check if account is active
     * @param email the email address
     * @param isActive the active status
     * @return Optional containing the user if found and active
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.isActive = :isActive")
    Optional<UserEntity> findByEmailAndIsActive(@Param("email") String email, @Param("isActive") Boolean isActive);
}
