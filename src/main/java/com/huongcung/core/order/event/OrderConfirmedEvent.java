package com.huongcung.core.order.event;

import com.huongcung.core.order.model.entity.OrderEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderConfirmedEvent extends ApplicationEvent {

    private final OrderEntity order;

    public OrderConfirmedEvent(Object source, OrderEntity order) {
        super(source);
        this.order = order;
    }
}
