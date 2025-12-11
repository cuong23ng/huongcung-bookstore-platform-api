package com.huongcung.businessmanagement.admin.service;

import com.huongcung.businessmanagement.admin.dto.AdminOrderDTO;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentDTO;
import com.huongcung.businessmanagement.fulfillment.model.ConsignmentEntryDTO;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.utils.AddressUtils;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.webstore.customer.dto.OrderDetailsDTO;
import com.huongcung.webstore.customer.dto.OrderItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {
    
    private final OrderRepository orderRepository;
    
    /**
     * Get all orders with pagination and filters for admin
     * 
     * @param pageable pagination parameters
     * @param status optional status filter
     * @param city optional city filter (based on shipping address)
     * @return paginated orders
     */
    @Transactional(readOnly = true)
    public Page<AdminOrderDTO> getAllOrders(Pageable pageable, OrderStatus status, City city) {
        log.debug("Admin fetching all orders - status: {}, city: {}, page: {}, size: {}", 
                status, city, pageable.getPageNumber(), pageable.getPageSize());
        
        // For now, we'll filter by status only
        // City filtering would require parsing shipping address JSON, which is more complex
        Page<OrderEntity> orders;
        if (status != null) {
            orders = orderRepository.findAllWithFilters(status, pageable);
        } else {
            // Default sort by createdAt descending if no sort is specified
            Pageable sortedPageable = pageable.getSort().isSorted() 
                ? pageable 
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                    Sort.by(Sort.Direction.DESC, "createdAt"));
            orders = orderRepository.findAll(sortedPageable);
        }
        
        return orders.map(this::toAdminOrderDTO);
    }
    
    /**
     * Get order details by ID for admin
     * 
     * @param orderId the order ID
     * @return order details
     */
    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrderDetails(Long orderId) {
        log.debug("Admin fetching order details for order: {}", orderId);
        
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        return toOrderDetailsDTO(order);
    }
    
    private AdminOrderDTO toAdminOrderDTO(OrderEntity order) {
        int itemCount = order.getEntries() != null 
            ? order.getEntries().stream()
                .mapToInt(entry -> entry.getQuantity() != null ? entry.getQuantity() : 0)
                .sum()
            : 0;
        
        Long customerId = null;
        String customerName = null;
        String customerEmail = null;
        if (order.getCustomer() != null) {
            customerId = order.getCustomer().getId();
            customerName = (order.getCustomer().getFirstName() != null ? order.getCustomer().getFirstName() : "") 
                + " " + (order.getCustomer().getLastName() != null ? order.getCustomer().getLastName() : "");
            customerEmail = order.getCustomer().getEmail();
        } else if (order.getOrderCustomer() != null) {
            customerName = order.getOrderCustomer().getFullName();
            customerEmail = order.getOrderCustomer().getEmail();
        }
        
        return AdminOrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(customerId)
            .customerName(customerName != null ? customerName.trim() : null)
            .customerEmail(customerEmail)
            .createdAt(order.getCreatedAt())
            .status(order.getStatus().name())
            .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING")
            .totalAmount(order.getTotalAmount())
            .itemCount(itemCount)
            .build();
    }
    
    private OrderDetailsDTO toOrderDetailsDTO(OrderEntity order) {
        // Convert consignments
        List<ConsignmentDTO> consignments = order.getConsignments() != null
            ? order.getConsignments().stream()
                .map(this::toConsignmentDTO)
                .collect(Collectors.toList())
            : java.util.Collections.emptyList();
        
        // Items are now included in consignments, but we can keep items for backward compatibility
        List<OrderItemDTO> items = order.getEntries() != null
            ? order.getEntries().stream()
                .map(entry -> OrderItemDTO.builder()
                    .id(entry.getId())
                    .bookCode(entry.getBook() != null ? entry.getBook().getCode() : null)
                    .bookTitle(entry.getBook() != null ? entry.getBook().getTitle() : "Unknown")
                    .itemType(entry.getItemType() != null ? entry.getItemType().name() : "PHYSICAL")
                    .quantity(entry.getQuantity() != null ? entry.getQuantity() : 0)
                    .unitPrice(entry.getUnitPrice() != null ? entry.getUnitPrice() : java.math.BigDecimal.ZERO)
                    .totalPrice(entry.getTotalPrice() != null ? entry.getTotalPrice() : java.math.BigDecimal.ZERO)
                    .build())
                .collect(Collectors.toList())
            : java.util.Collections.emptyList();
        
        // Parse shipping address using AddressUtils
        AddressDTO shippingAddressDTO = null;
        if (order.getShippingAddress() != null && !order.getShippingAddress().isBlank()) {
            shippingAddressDTO = AddressUtils.parseAddressJson(order.getShippingAddress());
        }
        
        return OrderDetailsDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .status(order.getStatus().name())
            .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : "PENDING")
            .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
            .orderType(order.getOrderType() != null ? order.getOrderType().name() : "MIXED")
            .subtotal(order.getSubtotal() != null ? order.getSubtotal() : java.math.BigDecimal.ZERO)
            .shippingAmount(order.getShippingAmount() != null ? order.getShippingAmount() : java.math.BigDecimal.ZERO)
            .taxAmount(order.getTaxAmount() != null ? order.getTaxAmount() : java.math.BigDecimal.ZERO)
            .discountAmount(order.getDiscountAmount() != null ? order.getDiscountAmount() : java.math.BigDecimal.ZERO)
            .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : java.math.BigDecimal.ZERO)
            .shippingAddress(shippingAddressDTO)
            .billingAddress(order.getBillingAddress())
            .notes(order.getNotes())
            .items(items)
            .deliveryInfo(null) // Can be populated if needed
            .consignments(consignments)
            .build();
    }
    
    /**
     * Convert ConsignmentEntity to ConsignmentDTO (public method for use in controllers)
     */
    public ConsignmentDTO toConsignmentDTO(ConsignmentEntity consignment) {
        WarehouseEntity warehouse = consignment.getOriginWarehouse();
        
        // Get customer info from order
        String customerName = "Unknown";
        String customerEmail = null;
        OrderEntity order = consignment.getOrder();
        if (order != null) {
            if (order.getCustomer() != null) {
                customerName = (order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName()).trim();
                customerEmail = order.getCustomer().getEmail();
            } else if (order.getOrderCustomer() != null) {
                customerName = order.getOrderCustomer().getFullName();
                customerEmail = order.getOrderCustomer().getEmail();
            }
        }
        
        // Convert entries
        List<ConsignmentEntryDTO> entryDTOs = new ArrayList<>();
        if (consignment.getEntries() != null) {
            for (ConsignmentEntryEntity entry : consignment.getEntries()) {
                OrderEntryEntity orderEntry = entry.getOrderEntry();
                if (orderEntry != null && orderEntry.getBook() != null) {
                    entryDTOs.add(ConsignmentEntryDTO.builder()
                            .id(entry.getId())
                            .orderEntryId(orderEntry.getId())
                            .bookId(orderEntry.getBook().getId())
                            .bookTitle(orderEntry.getBook().getTitle())
                            .bookCode(orderEntry.getBook().getCode())
                            .quantity(entry.getQuantity())
                            .shippedQuantity(entry.getShippedQuantity())
                            .unitPrice(orderEntry.getUnitPrice())
                            .totalPrice(orderEntry.getTotalPrice())
                            .build());
                }
            }
        }
        
        return ConsignmentDTO.builder()
                .id(consignment.getId())
                .code(consignment.getCode())
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .status(consignment.getStatus())
                .trackingNumber(consignment.getTrackingNumber())
                //.shippingCompany(consignment.getShippingCompany())
                .estimatedDeliveryDate(consignment.getEstimatedDeliveryDate())
                .actualDeliveryDate(consignment.getActualDeliveryDate())
                .shippingAddress(consignment.getShippingAddress())
                .notes(consignment.getNotes())
                .totalPrice(consignment.getTotalPrice())
                .codAmount(consignment.getCodAmount())
                .warehouseCity(warehouse != null ? warehouse.getCity() : null)
                .warehouseId(warehouse != null ? warehouse.getId() : null)
                .warehouseCode(warehouse != null ? warehouse.getCode() : null)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .entries(entryDTOs)
                .createdAt(consignment.getCreatedAt())
                .updatedAt(consignment.getUpdatedAt())
                .build();
    }
}
