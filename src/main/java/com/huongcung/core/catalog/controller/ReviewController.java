package com.huongcung.core.catalog.controller;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.dto.ReviewSourceDTO;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.external.service.AiReviewService;
import com.huongcung.core.catalog.model.entity.ReviewSource;
import com.huongcung.core.catalog.service.ReviewService;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import com.huongcung.businessmanagement.admin.model.request.ReviewCreateRequest;
import com.huongcung.businessmanagement.admin.model.request.ReviewUpdateRequest;
import com.huongcung.businessmanagement.admin.model.request.ReviewEnhanceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final AiReviewService aiReviewService;
    private final ReviewService reviewService;

    /**
     * Get review for a book (OneToOne relationship)
     */
    @GetMapping("/book/{bookId}")
    public ResponseEntity<BaseResponse> getReviewByBookId(
            @PathVariable Long bookId,
            @RequestParam(required = false) ReviewStatus status) {
        
        Optional<ReviewEntity> reviewOpt;
        if (status != null) {
            reviewOpt = reviewService.getReviewByBookIdAndStatus(bookId, status);
        } else {
            reviewOpt = reviewService.getReviewByBookId(bookId);
        }
        
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .data(null)
                    .message("No review found for this book")
                    .build());
        }
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(mapToResponse(reviewOpt.get()))
                .build());
    }

    /**
     * Create a manual review for a book
     */
    @PostMapping("/book/{bookId}")
    public ResponseEntity<BaseResponse> createReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewCreateRequest request) {
        
        // Ensure bookId matches
        request.setBookId(bookId);

        ReviewResponse review = reviewService.createReview(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.builder()
                .data(review)
                .message("Review created successfully")
                .build());
    }

    /**
     * Update an existing review for a book
     */
    @PutMapping("/book/{bookId}")
    public ResponseEntity<BaseResponse> updateReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewUpdateRequest request) {
        
        ReviewResponse review = reviewService.updateReview(bookId, request);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(review)
                .message("Review updated successfully")
                .build());
    }

    /**
     * Generate AI review for a book (creates new or replaces existing)
     */
    @PostMapping("/generate-ai/{bookId}")
    public ResponseEntity<BaseResponse> triggerAiReview(@PathVariable Long bookId) {

        aiReviewService.generateReviewAsync(bookId);

        return ResponseEntity.accepted().body(
                BaseResponse.builder()
                        .message("Yêu cầu tạo Review AI đã được tiếp nhận. Vui lòng kiểm tra lại sau 1-2 phút.")
                        .build()
        );
    }

    /**
     * Enhance existing review using AI (improve, expand, or shorten)
     */
    @PostMapping("/enhance-ai/{bookId}")
    public ResponseEntity<BaseResponse> enhanceReview(
            @PathVariable Long bookId,
            @RequestBody(required = false) ReviewEnhanceRequest request) {
        
        if (request == null) {
            request = new ReviewEnhanceRequest();
            request.setEnhancementType("improve");
        }
        
        // Gọi hàm async, không chờ kết quả
        aiReviewService.enhanceReviewAsync(bookId, request);

        // Trả về ngay lập tức
        return ResponseEntity.accepted().body(
                BaseResponse.builder()
                        .message("Yêu cầu cải thiện Review AI đã được tiếp nhận. Vui lòng kiểm tra lại sau 1-2 phút.")
                        .build()
        );
    }

    /**
     * Approve a review
     */
    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<BaseResponse> approveReview(@PathVariable Long reviewId) {
        ReviewEntity review = reviewService.approveReview(reviewId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(mapToResponse(review))
                .message("Review approved successfully")
                .build());
    }

    /**
     * Reject a review
     */
    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<BaseResponse> rejectReview(@PathVariable Long reviewId) {
        ReviewEntity review = reviewService.rejectReview(reviewId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(mapToResponse(review))
                .message("Review rejected successfully")
                .build());
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
                .title(entity.getTitle())
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
