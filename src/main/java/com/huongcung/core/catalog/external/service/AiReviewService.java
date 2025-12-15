package com.huongcung.core.catalog.external.service;

import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.businessmanagement.admin.model.request.ReviewEnhanceRequest;

import java.util.concurrent.CompletableFuture;

public interface AiReviewService {
    /**
     * Generate a new AI review for a book (creates new or replaces existing)
     */
    CompletableFuture<ReviewEntity> generateReviewAsync(Long bookId);
    
    /**
     * Enhance existing review using AI:
     * - improve: Improve and polish the existing review
     * - expand: Expand the review significantly with more depth and analysis
     * - shorten: Shorten and condense the review while keeping key points
     */
    CompletableFuture<ReviewEntity> enhanceReviewAsync(Long bookId, ReviewEnhanceRequest request);
}
