package com.huongcung.core.catalog.service;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.entity.ReviewEntity;

import java.util.List;

public interface ReviewService {
    List<ReviewEntity> getReviewsByBookId(Long bookId);
    List<ReviewEntity> getReviewsByBookIdAndStatus(Long bookId, ReviewStatus status);
    ReviewEntity approveReview(Long reviewId);
    ReviewEntity rejectReview(Long reviewId);
}



