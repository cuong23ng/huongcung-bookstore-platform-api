package com.huongcung.core.logistics.service.impl;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.logistics.enumeration.ConsignmentStatus;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.logistics.repository.ConsignmentEntityRepository;
import com.huongcung.core.logistics.repository.ConsignmentRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.logistics.service.LogisticsService;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final ConsignmentRepository consignmentRepository;
    private final ConsignmentEntityRepository consignmentEntityRepository;
    private final GhnService ghnService;

    @Transactional
    @Override
    public void fulfillOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId).orElseThrow();
        List<ConsignmentEntity> consignments = splitOrderStrategy(order);

        for (ConsignmentEntity consignment : consignments) {
            String trackingCode = ghnService.createShippingOrder(consignment);

            consignment.setTrackingNumber(trackingCode);
            consignment.setStatus(ConsignmentStatus.PENDING);
            consignmentRepository.save(consignment);
        }

        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
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

    private List<ConsignmentEntity> splitOrderStrategy(OrderEntity order) {
        //Chạy chiến lược chia kho (Hiện tại làm đơn giản: 1 Order -> 1 Consignment)
        //TODO: Thay đoạn này bằng thuật toán Backtracking/Greedy
        ConsignmentEntity consignment = new ConsignmentEntity();
        consignment.setOrder(order);
        consignment.setTotalPrice(order.getTotalAmount());
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            consignment.setCodAmount(consignment.getTotalPrice());
        } else {
            consignment.setCodAmount(BigDecimal.ZERO);
        }
        consignment.setCode(order.getOrderNumber() + "_1");
        consignment.setShippingAddress(order.getShippingAddress());
        WarehouseEntity warehouse = warehouseRepository.findByCity(City.HANOI).get(0);
        consignment.setOriginWarehouse(warehouse);
        consignment = consignmentRepository.save(consignment);
        List<ConsignmentEntryEntity> consignmentEntries = new ArrayList<>();
        for (OrderEntryEntity orderEntry : order.getEntries()) {
            ConsignmentEntryEntity consignmentEntry = new ConsignmentEntryEntity();
            consignmentEntry.setOrderEntry(orderEntry);
            consignmentEntry.setConsignment(consignment);
            consignmentEntry.setQuantity(orderEntry.getQuantity());
            consignmentEntry.setShippedQuantity(0);
            consignmentEntries.add(consignmentEntry);
        }
        consignment.setEntries(consignmentEntries);
        consignmentEntries = consignmentEntityRepository.saveAll(consignmentEntries);
        return List.of(consignment);
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
}
