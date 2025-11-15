package com.huongcung.webstore.customer.service;

import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.model.entity.DeliveryInfoEntity;
import com.huongcung.webstore.customer.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryService {
    
    private final OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public Page<OrderHistoryDTO> getOrderHistory(Long customerId, Pageable pageable) {
        log.debug("Fetching order history for customer: {}", customerId);
        
        Page<OrderEntity> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);
        
        return orders.map(this::toOrderHistoryDTO);
    }
    
    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrderDetails(Long orderId, Long customerId) {
        log.debug("Fetching order details for order: {} by customer: {}", orderId, customerId);
        
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        // Verify customer owns the order
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new SecurityException("Access denied: Order does not belong to customer");
        }
        
        return toOrderDetailsDTO(order);
    }
    
    private OrderHistoryDTO toOrderHistoryDTO(OrderEntity order) {
        int itemCount = order.getEntries() != null 
            ? order.getEntries().stream()
                .mapToInt(OrderEntryEntity::getQuantity)
                .sum()
            : 0;
        
        return OrderHistoryDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .createdAt(order.getCreatedAt())
            .status(order.getStatus().name())
            .paymentStatus(order.getPaymentStatus().name())
            .totalAmount(order.getTotalAmount())
            .itemCount(itemCount)
            .build();
    }
    
    private OrderDetailsDTO toOrderDetailsDTO(OrderEntity order) {
        List<OrderItemDTO> items = order.getEntries() != null
            ? order.getEntries().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList())
            : java.util.Collections.emptyList();
        
        DeliveryInfoDTO deliveryInfo = null;
        if (order.getDeliveryInfo() != null) {
            deliveryInfo = toDeliveryInfoDTO(order.getDeliveryInfo());
        }
        
        return OrderDetailsDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .status(order.getStatus().name())
            .paymentStatus(order.getPaymentStatus().name())
            .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
            .orderType(order.getOrderType().name())
            .subtotal(order.getSubtotal())
            .shippingAmount(order.getShippingAmount())
            .taxAmount(order.getTaxAmount())
            .discountAmount(order.getDiscountAmount())
            .totalAmount(order.getTotalAmount())
            .shippingAddress(order.getShippingAddress())
            .billingAddress(order.getBillingAddress())
            .notes(order.getNotes())
            .items(items)
            .deliveryInfo(deliveryInfo)
            .build();
    }
    
    private OrderItemDTO toOrderItemDTO(OrderEntryEntity entry) {
        return OrderItemDTO.builder()
            .id(entry.getId())
            .bookCode(entry.getBook().getCode())
            .bookTitle(entry.getBook().getTitle())
            .itemType(entry.getItemType().name())
            .quantity(entry.getQuantity())
            .unitPrice(entry.getUnitPrice())
            .totalPrice(entry.getTotalPrice())
            .build();
    }
    
    private DeliveryInfoDTO toDeliveryInfoDTO(DeliveryInfoEntity deliveryInfo) {
        return DeliveryInfoDTO.builder()
            .provinceId(deliveryInfo.getProvinceId())
            .districtId(deliveryInfo.getDistrictId())
            .wardCode(deliveryInfo.getWardCode())
            .serviceTypeId(deliveryInfo.getServiceTypeId())
            .serviceId(deliveryInfo.getServiceId())
            .expectedDeliveryTime(deliveryInfo.getExpectedDeliveryTime())
            .ghnOrderCode(deliveryInfo.getGhnOrderCode())
            .weight(deliveryInfo.getWeight())
            .length(deliveryInfo.getLength())
            .width(deliveryInfo.getWidth())
            .height(deliveryInfo.getHeight())
            .build();
    }
}

