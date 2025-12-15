package com.huongcung.core.catalog.model.dto.response;

import com.huongcung.core.catalog.enumeration.ReviewStatus;
import com.huongcung.core.catalog.model.dto.ReviewSourceDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private Long bookId;
    private Long userId;
    private String title;           // Tiêu đề bài review (AI tự đặt cho hấp dẫn)
    private String content;         // Nội dung
    private Integer suggestedRating;// Điểm số AI đề xuất (1-5)
    private List<ReviewSourceDTO> sources;   // Nguồn tham khảo
    private String sentiment;
    private Boolean isAiGenerated;
    private ReviewStatus status;
}
