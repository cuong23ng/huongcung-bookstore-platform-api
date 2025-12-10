package com.huongcung.core.catalog.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookReviewDTO {
    private String title;
    private String content;
    private List<ReviewSourceDTO> sources;
}
