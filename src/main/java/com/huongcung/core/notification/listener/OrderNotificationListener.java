package com.huongcung.core.notification.listener;

import com.huongcung.core.common.utils.AddressUtils;
import com.huongcung.core.contributor.model.entity.AuthorEntity;
import com.huongcung.core.notification.dto.OrderItemDTO;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.notification.dto.OrderPlacedEvent;
import com.huongcung.core.order.model.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        OrderEntity order = event.getOrder();

        // 1. Map Entity sang DTO (chỉ gửi dữ liệu cần thiết, không gửi cả Entity to đùng)
        OrderPlacedEvent message = new OrderPlacedEvent();
        message.setOrderNumber(order.getOrderNumber());
        message.setCustomerEmail(order.getOrderCustomer().getEmail());
        message.setCustomerName(order.getOrderCustomer().getFullName());
        message.setTotalAmount(order.getTotalAmount());
        message.setOrderDate(order.getCreatedAt());
        message.setPaymentMethod(order.getPaymentMethod().getName());
        message.setShippingAddress(AddressUtils.formatAddressString(order.getShippingAddress()));
        List<OrderItemDTO> orderItems = order.getEntries().parallelStream().map(oe -> {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setBookTitle(oe.getBook().getTitle());
            orderItem.setSubTotal(oe.getTotalPrice());
            orderItem.setQuantity(oe.getQuantity());
            orderItem.setAuthors(oe.getBook().getAuthors().parallelStream().map(AuthorEntity::getName).toList());
            return orderItem;
        }).toList();
        message.setSubTotal(order.getSubtotal());
        message.setOrderItems(orderItems);

        // 2. Bắn vào Kafka Topic "order-placed-topic"
        log.info("Bắn sự kiện sang Kafka cho đơn: {} - Email: {}", order.getOrderNumber(), message.getCustomerEmail());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("order-placed-topic", order.getOrderNumber(), message);

        future.thenAccept(result -> 
            log.info("Gửi message thành công cho đơn: {} - Topic: {}, Partition: {}, Offset: {}", 
                order.getOrderNumber(), 
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset())
        ).exceptionally(ex -> {
            log.error("Lỗi khi gửi message cho đơn: {} - Lỗi: {}", order.getOrderNumber(), ex.getMessage(), ex);
            return null;
        });
    }
}
