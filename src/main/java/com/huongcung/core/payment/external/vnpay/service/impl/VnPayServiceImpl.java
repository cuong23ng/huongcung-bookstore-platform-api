package com.huongcung.core.payment.external.vnpay.service.impl;

import com.huongcung.core.payment.configuration.VnpayConfig;
import com.huongcung.core.payment.external.vnpay.service.VnPayService;
import com.huongcung.core.payment.service.PaymentConfirmationService;
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
public class VnPayServiceImpl implements VnPayService {

    private final PaymentConfirmationService paymentConfirmationService;
    private final VnpayConfig vnpayConfig;

    public String createPaymentUrl(String orderNumber, Long amount, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = orderNumber; // Mã đơn hàng
        String vnp_IpAddr = ipAddress;
        String vnp_TmnCode = vnpayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh_Toan_Don_Hang:" + vnp_TxnRef);
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
                // 1. Encode chuẩn UTF-8
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8);

                // 2. FIX QUAN TRỌNG: Thay thế '+' bằng '%20'
                // VNPay thường yêu cầu khoảng trắng là %20 để tính checksum chính xác
                encodedValue = encodedValue.replace("+", "%20");

                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(encodedValue); // Dùng giá trị đã fix

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(encodedValue); // Dùng giá trị đã fix

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        log.info("VNPAY Raw Hash: {}", hashData);
        log.info("Secret Key: {}", vnpayConfig.secretKey);

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
    @Override
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

        String signValue = VnpayConfig.hmacSHA512(vnpayConfig.secretKey, hashAllFields(fields));

        if (signValue.equals(vnp_SecureHash)) {
            String orderNumber = fields.get("vnp_TxnRef");
            String responseCode = fields.get("vnp_ResponseCode");

            long vnpAmount = Long.parseLong(fields.get("vnp_Amount"));
            long amount = vnpAmount / 100;

            if (!paymentConfirmationService.checkValidOrderNumber(orderNumber)) {
                return Map.of("RspCode", "01", "Message", "Order not Found");
            }

            if (!paymentConfirmationService.checkReceivedAmountForOrder(orderNumber, amount)) {
                return Map.of("RspCode", "04", "Message", "Invalid Amount");
            }

            if ("00".equals(responseCode)) {
                log.info("Payment Success for Order: {}", orderNumber);
                paymentConfirmationService.handlePaymentSuccess(orderNumber);
            } else {
                log.info("Payment Failed for Order: {} with code: {}", orderNumber, responseCode);
                // Có thể gọi orderService.handlePaymentFailure(order.getId()) nếu cần
            }

            return Map.of("RspCode", "00", "Message", "Confirm Success");

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
