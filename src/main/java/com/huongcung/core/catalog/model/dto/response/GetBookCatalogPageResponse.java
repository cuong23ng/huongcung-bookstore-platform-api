package com.huongcung.core.catalog.model.dto.response;

import com.huongcung.core.catalog.model.dto.BookListDTO;
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
public class GetBookCatalogPageResponse {
    private List<BookListDTO> books;
    private PaginationInfo pagination;
}
