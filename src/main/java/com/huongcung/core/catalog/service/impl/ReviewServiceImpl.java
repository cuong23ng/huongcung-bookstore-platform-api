package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.dto.ReviewSourceDTO;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.model.entity.ReviewSource;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.repository.ReviewRepository;
import com.huongcung.core.catalog.service.ReviewService;
import com.huongcung.businessmanagement.admin.model.request.ReviewCreateRequest;
import com.huongcung.businessmanagement.admin.model.request.ReviewUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AbstractBookRepository bookRepository;

    @Override
    public Optional<ReviewEntity> getReviewByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public Optional<ReviewEntity> getReviewByBookIdAndStatus(Long bookId, ReviewStatus status) {
        return reviewRepository.findByBookIdAndStatus(bookId, status);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        AbstractBookEntity book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));
        
        // Check if review already exists (OneToOne constraint)
        Optional<ReviewEntity> existingReview = reviewRepository.findByBookId(request.getBookId());
        if (existingReview.isPresent()) {
            throw new RuntimeException("Review already exists for book with id: " + request.getBookId());
        }
        
        ReviewEntity review = new ReviewEntity();
        review.setBook(book);
        review.setRating(request.getRating());
        review.setComment(request.getContent());
        review.setIsAiGenerated(request.getIsAiGenerated() != null ? request.getIsAiGenerated() : false);
        review.setStatus(ReviewStatus.DRAFT);
        
        log.info("Creating review for book {}", request.getBookId());
        ReviewEntity reviewEntity = reviewRepository.save(review);
        book.setReview(reviewEntity);
        bookRepository.save(book);
        return mapToResponse(reviewEntity);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long bookId, ReviewUpdateRequest request) {
        ReviewEntity review = reviewRepository.findByBookId(bookId)
                .orElseThrow(() -> new RuntimeException("Review not found for book with id: " + bookId));
        
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getContent() != null) {
            review.setComment(request.getContent());
        }
        // Mark as manually edited if it was AI generated
        if (review.getIsAiGenerated() != null && review.getIsAiGenerated()) {
            // Keep isAiGenerated flag but mark that it's been edited
            log.info("Review for book {} was AI generated but has been manually edited", bookId);
        }
        
        log.info("Updating review for book {}", bookId);
        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewEntity getOrCreateReview(Long bookId) {
        return reviewRepository.findByBookId(bookId)
                .orElseGet(() -> {
                    AbstractBookEntity book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));
                    
                    ReviewEntity newReview = new ReviewEntity();
                    newReview.setBook(book);
                    newReview.setStatus(ReviewStatus.DRAFT);
                    newReview.setIsAiGenerated(false);
                    
                    log.info("Creating new review for book {}", bookId);
                    return reviewRepository.save(newReview);
                });
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

    // Legacy methods for backward compatibility
    @Override
    @Deprecated
    public List<ReviewEntity> getReviewsByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId)
                .map(List::of)
                .orElse(List.of());
    }

    @Override
    @Deprecated
    public List<ReviewEntity> getReviewsByBookIdAndStatus(Long bookId, ReviewStatus status) {
        return reviewRepository.findByBookIdAndStatus(bookId, status)
                .map(List::of)
                .orElse(List.of());
    }

    private ReviewResponse mapToResponse(ReviewEntity entity) {
        // Convert ReviewSource to ReviewSourceDTO
        List<ReviewSourceDTO> sourceDTOs = new ArrayList<>();
        if (entity.getSources() != null) {
            // Force load by copying to new ArrayList
            List<ReviewSource> sources = new ArrayList<>(entity.getSources());
            for (ReviewSource reviewSource : sources) {
                ReviewSourceDTO sourceDTO = ReviewSourceDTO.builder()
                        .title(reviewSource.getTitle())
                        .url(reviewSource.getUrl())
                        .build();
                sourceDTOs.add(sourceDTO);
            }
        }
        
        return ReviewResponse.builder()
                .id(entity.getId())
                .bookId(entity.getBook() != null ? entity.getBook().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .suggestedRating(entity.getRating())
                .content(entity.getComment())
                .isAiGenerated(entity.getIsAiGenerated())
                .sources(sourceDTOs)
                .status(entity.getStatus())
                .build();
    }
}



