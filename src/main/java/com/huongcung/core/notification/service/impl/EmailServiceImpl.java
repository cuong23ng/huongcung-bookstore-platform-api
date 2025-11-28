package com.huongcung.core.notification.service.impl;

import com.huongcung.core.notification.service.EmailService;
import com.huongcung.core.order.model.entity.OrderEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendOrderConfirmationEmail(OrderEntity order) {
        log.info("Sending confirmation email for order: {}", order.getOrderNumber());
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getOrderCustomer().getEmail());
            helper.setSubject("Hương Cung Bookstore - Xác nhận đơn hàng #" + order.getOrderNumber());

            // TODO: Template Engine (như Thymeleaf) để tạo nội dung HTML
            String htmlContent = "<h1>Cảm ơn bạn đã đặt hàng!</h1>"
                    + "<p>Mã đơn hàng: <b>" + order.getOrderNumber() + "</b></p>"
                    + "<p>Tổng tiền: " + order.getTotalAmount() + "</p>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", order.getOrderCustomer().getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email for order {}", order.getOrderNumber(), e);
        }
    }
}
