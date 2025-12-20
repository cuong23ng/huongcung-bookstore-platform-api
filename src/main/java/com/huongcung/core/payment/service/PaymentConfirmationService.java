package com.huongcung.core.payment.service;

public interface PaymentConfirmationService {

    void handlePaymentSuccess(String orderNumber);

    boolean checkValidOrderNumber(String orderNumber);

    boolean checkReceivedAmountForOrder(String orderNumber, Long amount);
}
