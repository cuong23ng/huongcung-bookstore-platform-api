package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.repository.ReviewRepository;
import com.huongcung.core.catalog.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public List<ReviewEntity> getReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public List<ReviewEntity> getReviewsByBookIdAndStatus(Long bookId, ReviewStatus status) {
        return reviewRepository.findByBookIdAndStatus(bookId, status);
    }

    @Override
    @Transactional
    public ReviewEntity approveReview(Long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        review.setStatus(ReviewStatus.PUBLISHED);
        log.info("Review {} approved", reviewId);
        
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public ReviewEntity rejectReview(Long reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        
        review.setStatus(ReviewStatus.REJECTED);
        log.info("Review {} rejected", reviewId);
        
        return reviewRepository.save(review);
    }
}



