package com.huongcung.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private String orderNumber;
    private String customerEmail;
    private String customerName;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String paymentMethod;
    private BigDecimal subTotal;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> orderItems;
}
