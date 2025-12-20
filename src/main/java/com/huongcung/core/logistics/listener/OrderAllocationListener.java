package com.huongcung.core.logistics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.order.event.OrderConfirmedEvent;
import com.huongcung.core.order.model.dto.AllocationPlanDTO;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.order.strategy.SplitOrderStrategy;
import com.huongcung.storefront.checkout.dto.ShippingAddressDTO;
import com.huongcung.storefront.checkout.utils.AddressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAllocationListener {

    private final SplitOrderStrategy splitOrderStrategy;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        OrderEntity order = event.getOrder();

        ShippingAddressDTO shippingAddress = AddressUtils.parseShippingAddressJson(order.getShippingAddress());
        AddressDTO customerAddress = new AddressDTO();
        AddressUtils.populateAddress(shippingAddress, customerAddress);
        AllocationPlanDTO allocationPlan = splitOrderStrategy.simulateSplitOrder(order.getEntries(), customerAddress);
        try {
            order.setAllocationPlan(objectMapper.writeValueAsString(allocationPlan));
            orderRepository.save(order);
            log.info("Order {} is allocated", order.getOrderNumber());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
