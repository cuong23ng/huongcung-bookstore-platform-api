package com.huongcung.core.catalog.repository;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByBookId(Long bookId);
    List<ReviewEntity> findByBookIdAndStatus(Long bookId, ReviewStatus status);
    List<ReviewEntity> findByStatus(ReviewStatus status);
}
