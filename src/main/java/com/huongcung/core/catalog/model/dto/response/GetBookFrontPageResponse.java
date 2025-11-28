package com.huongcung.core.catalog.model.dto.response;

import com.huongcung.core.catalog.model.dto.BookFrontPageDTO;
import com.huongcung.core.search.model.dto.PaginationInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetBookFrontPageResponse {
    private List<BookFrontPageDTO> books;
    private PaginationInfo pagination;
    private Long executionTimeMs;
}
