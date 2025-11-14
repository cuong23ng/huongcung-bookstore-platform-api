package com.huongcung.platform.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDTO {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private String status;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private Integer itemCount;
}

