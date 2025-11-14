package com.huongcung.businessmanagement.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for publisher list responses (paginated)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublisherListDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

