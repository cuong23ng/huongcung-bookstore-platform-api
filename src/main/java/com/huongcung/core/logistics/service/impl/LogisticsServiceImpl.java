package com.huongcung.core.logistics.service.impl;

import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.dto.ShippingOrderDTO;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.logistics.service.DeliveryService;
import com.huongcung.core.logistics.service.LogisticsService;
import com.huongcung.core.logistics.strategy.SplitOrderStrategy;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderRepository orderRepository;
    private final ConsignmentRepository consignmentRepository;
    private final DeliveryService deliveryService;
    private final SplitOrderStrategy splitOrderStrategy;

    /**
     * Plan fulfillment - create consignments without creating shipping orders
     * 
     * @param orderId the order ID
     * @return list of created consignments
     */
    @Transactional
    public List<ConsignmentEntity> planFulfillment(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        List<ConsignmentEntity> consignments = splitOrderStrategy.splitOrder(order);

        // Set status to CREATED (not yet sent shipping order request)
        for (ConsignmentEntity consignment : consignments) {
            consignment.setStatus(ConsignmentStatus.CREATED);
            consignmentRepository.save(consignment);
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        
        return consignments;
    }

    /**
     * Create shipping order for a consignment
     * This method sends a request to GHN to create a shipping order
     * 
     * @param consignmentId the consignment ID
     * @return tracking number from GHN
     */
    @Transactional
    public String createShippingOrderForConsignment(Long consignmentId) {
        ConsignmentEntity consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Consignment not found: " + consignmentId));

        if (consignment.getStatus() != ConsignmentStatus.CREATED) {
            throw new IllegalStateException(
                    "Consignment must be in CREATED status to create shipping order. Current status: " + consignment.getStatus());
        }

        ShippingOrderDTO shippingOrder = deliveryService.createShippingOrder(consignment);
        consignment.setTrackingNumber(shippingOrder.getOrderCode());
        
        // Parse expected delivery time - handle multiple formats
        LocalDateTime estimatedDeliveryDate = parseExpectedDeliveryTime(shippingOrder.getExpectedDeliveryTime());
        consignment.setEstimatedDeliveryDate(estimatedDeliveryDate);
        
        consignment.setShippingAmount(BigDecimal.valueOf(shippingOrder.getTotalFee()));

        consignment.setStatus(ConsignmentStatus.PENDING);
        consignment.setTotalPrice(consignment.getSubTotal().add(consignment.getShippingAmount()));
        consignmentRepository.save(consignment);

        return consignment.getTrackingNumber();
    }

    @Override
    public void updateConsignmentStatusByTrackingNumber(String trackingNumber, ConsignmentStatus status) {
        ConsignmentEntity consignment = consignmentRepository.findByTrackingNumber(trackingNumber).orElse(null);

        if (consignment == null) {
            log.warn("Tracking number not found: {}", trackingNumber);
            return;
        }
        consignment.setStatus(status);
        consignmentRepository.save(consignment);
        updateParentOrderStatus(consignment.getOrder());
    }

    private void updateParentOrderStatus(OrderEntity order) {
        List<ConsignmentEntity> allConsignments = order.getConsignments();

        boolean allDelivered = allConsignments.stream()
                .allMatch(c -> c.getStatus() == ConsignmentStatus.DELIVERED);

        if (allDelivered) {
            // Nếu đơn hàng đã giao xong hết -> Update Order thành DELIVERED
            // (Nếu có Ebook, cần check thêm logic Ebook đã gửi chưa)
            if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.COMPLETED) {
                order.setStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
                log.info("Order {} is fully delivered", order.getOrderNumber());

                // TODO: Bắn Event gửi mail "Giao hàng thành công" cho khách
            }
        }

        // Có thể thêm logic: Nếu có 1 gói bị RETURNED -> Order chuyển sang trạng thái cảnh báo
    }

    /**
     * Parse expected delivery time string to LocalDateTime
     * Handles multiple formats: ISO_OFFSET_DATE_TIME (with Z), ISO_LOCAL_DATE_TIME, ISO_LOCAL_DATE
     */
    private LocalDateTime parseExpectedDeliveryTime(String expectedDeliveryTime) {
        if (expectedDeliveryTime == null || expectedDeliveryTime.trim().isEmpty()) {
            return null;
        }
        
        String timeStr = expectedDeliveryTime.trim();
        
        // Try ISO_OFFSET_DATE_TIME first (handles 'Z' timezone)
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return zonedDateTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            // Continue to next format
        }
        
        // Try ISO_INSTANT (for UTC with Z)
        try {
            if (timeStr.endsWith("Z")) {
                java.time.Instant instant = java.time.Instant.parse(timeStr);
                return LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            }
        } catch (DateTimeParseException e) {
            // Continue to next format
        }
        
        // Try ISO_LOCAL_DATE_TIME
        try {
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Continue to next format
        }
        
        // Try ISO_LOCAL_DATE (date only)
        try {
            LocalDate date = LocalDate.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            log.warn("Cannot parse expected delivery time: {}, using null", timeStr);
            return null;
        }
    }

    public ConsignmentStatus mapGhnStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick" -> ConsignmentStatus.PENDING;
            case "picking", "picked" -> ConsignmentStatus.PICKED_UP;
            case "storing", "transporting", "sorting" -> ConsignmentStatus.IN_TRANSIT;
            case "delivering" -> ConsignmentStatus.OUT_FOR_DELIVERY;
            case "delivered" -> ConsignmentStatus.DELIVERED;
            case "delivery_fail" -> ConsignmentStatus.FAILED_DELIVERY;
            case "return", "returning", "returned" -> ConsignmentStatus.RETURNED;
            default -> null;
        };
    }
}
