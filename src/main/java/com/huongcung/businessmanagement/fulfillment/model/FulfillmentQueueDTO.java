package com.huongcung.businessmanagement.fulfillment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for fulfillment queue entries
 * Represents orders that can be fulfilled from a specific warehouse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FulfillmentQueueDTO {
    private Long orderId;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private Integer itemCount; // Total number of items in order
    private Integer fulfillableItemCount; // Number of items that can be fulfilled from this warehouse
    private List<FulfillableItemDTO> fulfillableItems; // Items that can be fulfilled from this warehouse
}


