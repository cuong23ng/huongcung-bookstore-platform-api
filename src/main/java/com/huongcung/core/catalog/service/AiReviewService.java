package com.huongcung.core.catalog.service;

import com.huongcung.core.catalog.model.entity.ReviewEntity;

import java.util.concurrent.CompletableFuture;

public interface AiReviewService {
    CompletableFuture<ReviewEntity> generateReviewAsync(Long bookId);
}
