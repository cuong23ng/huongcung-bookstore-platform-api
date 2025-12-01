package com.huongcung.core.payment.service.impl;

import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.service.OrderService;
import com.huongcung.core.payment.configuration.VnpayConfig;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final VnpayConfig vnpayConfig;

    public String createPaymentUrl(Long orderId, HttpServletRequest request) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = order.getOrderNumber(); // Mã đơn hàng
        String vnp_IpAddr = VnpayConfig.getIpAddress(request);
        String vnp_TmnCode = vnpayConfig.vnp_TmnCode;

        // Số tiền nhân 100 (theo quy định VNPay)
        long amount = order.getTotalAmount().longValue() * 100;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnpayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Expire date (15 phút)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build Query URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnpayConfig.hmacSHA512(vnpayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("Payment URL: {}", vnpayConfig.vnp_PayUrl + "?" + queryUrl);

        return vnpayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * Xử lý logic IPN từ VNPay
     * @param requestParams Map chứa các tham số VNPay gửi về
     * @return Map chứa RspCode và Message để trả về cho VNPay
     */
    @Transactional
    public Map<String, String> processIpn(Map<String, String> requestParams) {
        // 1. Lấy Secure Hash từ request và xóa các param không tham gia tạo hash
        String vnp_SecureHash = requestParams.get("vnp_SecureHash");

        // Tạo bản sao để xử lý, tránh sửa trực tiếp map gốc nếu cần dùng lại
        Map<String, String> fields = new HashMap<>(requestParams);
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // 2. Tính toán Checksum
        // Cần sắp xếp các field theo thứ tự bảng chữ cái trước khi hash (quan trọng)
        String signValue = VnpayConfig.hmacSHA512(vnpayConfig.secretKey, hashAllFields(fields));

        // 3. Xác thực Checksum
        if (signValue.equals(vnp_SecureHash)) {
            // Checksum hợp lệ
            String orderNumber = fields.get("vnp_TxnRef");
            String responseCode = fields.get("vnp_ResponseCode");
            // Số tiền từ VNPay đã nhân 100
            long vnpAmount = Long.parseLong(fields.get("vnp_Amount"));
            long amount = vnpAmount / 100;

            // 4. Tìm đơn hàng
            OrderEntity order = orderRepository.findByOrderNumber(orderNumber).orElse(null);

            if (order != null) {
                // 5. Kiểm tra số tiền (tránh gian lận sửa URL)
                if (order.getTotalAmount().longValue() == amount) {
                    if ("00".equals(responseCode)) {
                        // Thanh toán thành công
                        log.info("Payment Success for Order: {}", orderNumber);

                        // Gọi OrderService để cập nhật trạng thái đơn hàng + bắn sự kiện
                        orderService.handlePaymentSuccess(order.getId());
                    } else {
                        // Thanh toán thất bại
                        log.info("Payment Failed for Order: {} with code: {}", orderNumber, responseCode);
                        // Có thể gọi orderService.handlePaymentFailure(order.getId()) nếu cần
                    }
                    return Map.of("RspCode", "00", "Message", "Confirm Success");
                } else {
                    return Map.of("RspCode", "04", "Message", "Invalid Amount");
                }
            } else {
                return Map.of("RspCode", "01", "Message", "Order not Found");
            }
        } else {
            return Map.of("RspCode", "97", "Message", "Invalid Checksum");
        }
    }

    /**
     * Helper: Sắp xếp và nối chuỗi dữ liệu để tạo hash
     * TODO: tách ra để tái sử dụng
     */
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
}