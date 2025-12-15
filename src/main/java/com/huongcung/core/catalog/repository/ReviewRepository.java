package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    // OneToOne relationship - each book has only one review
    Optional<ReviewEntity> findByBookId(Long bookId);
    Optional<ReviewEntity> findByBookIdAndStatus(Long bookId, ReviewStatus status);
    
    // Legacy methods for backward compatibility (if needed)
    @Deprecated
    List<ReviewEntity> findByStatus(ReviewStatus status);
}
