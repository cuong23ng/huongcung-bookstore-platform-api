package com.huongcung.core.catalog.service;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.businessmanagement.admin.model.request.ReviewCreateRequest;
import com.huongcung.businessmanagement.admin.model.request.ReviewUpdateRequest;

import java.util.Optional;

public interface ReviewService {
    // OneToOne - get single review for a book
    Optional<ReviewEntity> getReviewByBookId(Long bookId);
    Optional<ReviewEntity> getReviewByBookIdAndStatus(Long bookId, ReviewStatus status);
    
    // CRUD operations
    ReviewResponse createReview(ReviewCreateRequest request);
    ReviewResponse updateReview(Long bookId, ReviewUpdateRequest request);
    ReviewEntity getOrCreateReview(Long bookId);
    
    // Approval workflow
    ReviewEntity approveReview(Long reviewId);
    ReviewEntity rejectReview(Long reviewId);
    
    // Legacy methods for backward compatibility
    @Deprecated
    java.util.List<ReviewEntity> getReviewsByBookId(Long bookId);
    @Deprecated
    java.util.List<ReviewEntity> getReviewsByBookIdAndStatus(Long bookId, ReviewStatus status);
}



