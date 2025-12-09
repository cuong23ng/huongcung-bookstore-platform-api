package com.huongcung.core.catalog.controller;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.entity.ReviewEntity;
import com.huongcung.core.catalog.model.dto.response.ReviewResponse;
import com.huongcung.core.catalog.service.AiReviewService;
import com.huongcung.core.catalog.service.ReviewService;
import com.huongcung.core.common.model.dto.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final AiReviewService aiReviewService;
    private final ReviewService reviewService;

    @PostMapping("/generate-ai/{bookId}")
    public ResponseEntity<BaseResponse> triggerAiReview(@PathVariable Long bookId) {
        // Gọi hàm async, không chờ kết quả
        aiReviewService.generateReviewAsync(bookId);

        // Trả về ngay lập tức
        return ResponseEntity.accepted().body(
                BaseResponse.builder()
                        .message("Yêu cầu tạo Review AI đã được tiếp nhận. Vui lòng kiểm tra lại sau 1-2 phút trong mục 'Duyệt bài'.")
                        .build()
        );
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<BaseResponse> getReviewsByBookId(
            @PathVariable Long bookId,
            @RequestParam(required = false) ReviewStatus status) {
        
        List<ReviewEntity> reviews;
        if (status != null) {
            reviews = reviewService.getReviewsByBookIdAndStatus(bookId, status);
        } else {
            reviews = reviewService.getReviewsByBookId(bookId);
        }
        
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(reviewResponses)
                .build());
    }

    @PutMapping("/{reviewId}/approve")
    public ResponseEntity<BaseResponse> approveReview(@PathVariable Long reviewId) {
        ReviewEntity review = reviewService.approveReview(reviewId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(mapToResponse(review))
                .message("Review approved successfully")
                .build());
    }

    @PutMapping("/{reviewId}/reject")
    public ResponseEntity<BaseResponse> rejectReview(@PathVariable Long reviewId) {
        ReviewEntity review = reviewService.rejectReview(reviewId);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .data(mapToResponse(review))
                .message("Review rejected successfully")
                .build());
    }

    private ReviewResponse mapToResponse(ReviewEntity entity) {
        return ReviewResponse.builder()
                .id(entity.getId())
                .bookId(entity.getBook() != null ? entity.getBook().getId() : null)
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .suggestedRating(entity.getRating())
                .content(entity.getComment())
                .isAiGenerated(entity.getIsAiGenerated())
                .sources(entity.getSources())
                .status(entity.getStatus())
                .build();
    }

}
