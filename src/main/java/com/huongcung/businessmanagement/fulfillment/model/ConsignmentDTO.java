package com.huongcung.businessmanagement.fulfillment.model;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for consignment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsignmentDTO {
    
    private Long id;
    private String code;
    private Long orderId;
    private String orderNumber;
    private ConsignmentStatus status;
    private String trackingNumber;
    private String shippingCompany;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String shippingAddress;
    private String notes;
    private BigDecimal totalPrice;
    private BigDecimal codAmount;
    private City warehouseCity;
    private Long warehouseId;
    private String warehouseCode;
    private String customerName;
    private String customerEmail;
    private List<ConsignmentEntryDTO> entries;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



