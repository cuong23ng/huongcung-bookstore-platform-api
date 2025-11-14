package com.huongcung.platform.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDTO {
    private Long id;
    private String orderNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String orderType;
    private BigDecimal subtotal;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String shippingAddress; // JSON string
    private String billingAddress; // JSON string
    private String notes;
    private List<OrderItemDTO> items;
    private DeliveryInfoDTO deliveryInfo;
}

