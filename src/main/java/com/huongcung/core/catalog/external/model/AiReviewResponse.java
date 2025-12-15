package com.huongcung.core.catalog.external.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiReviewResponse {
    private String title;               // Tiêu đề bài review (AI tự đặt cho hấp dẫn)
    private String content;             // Nội dung
    private Integer suggestedRating;    // Điểm số AI đề xuất (1-5)
    private List<SourceDTO> sources;    // Nguồn tham khảo
    private String sentiment;
}
