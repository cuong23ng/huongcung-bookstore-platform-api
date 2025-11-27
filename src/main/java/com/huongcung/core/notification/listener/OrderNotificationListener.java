package com.huongcung.core.notification.listener;

import com.huongcung.core.notification.service.EmailService;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for order: {}", event.getOrder().getOrderNumber());
        emailService.sendOrderConfirmationEmail(event.getOrder());
    }
}
