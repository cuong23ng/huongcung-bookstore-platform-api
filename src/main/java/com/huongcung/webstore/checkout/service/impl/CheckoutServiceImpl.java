package com.huongcung.webstore.checkout.service.impl;

import com.huongcung.core.catalog.enumeration.BookType;
import com.huongcung.core.inventory.service.InventoryService;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.order.enumeration.*;
import com.huongcung.core.order.model.entity.DeliveryInfoEntity;
import com.huongcung.core.order.model.entity.OrderCustomerEntity;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.DeliveryInfoRepository;
import com.huongcung.core.order.repository.OrderCustomerRepository;
import com.huongcung.core.order.repository.OrderEntryRepository;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.order.service.OrderConfirmationService;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.repository.CustomerRepository;
import com.huongcung.core.user.service.CustomerService;
import com.huongcung.webstore.checkout.dto.*;
import com.huongcung.webstore.checkout.external.ghn.GhnApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.webstore.checkout.service.CheckoutService;
import com.huongcung.webstore.checkout.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {
    
    private final OrderRepository orderRepository;
    private final OrderEntryRepository orderEntryRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final AbstractBookRepository abstractBookRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final DeliveryService deliveryService;
    private final InventoryService inventoryService;
    private final OrderConfirmationService orderConfirmationService;
    private final ObjectMapper objectMapper;
    private final WarehouseRepository warehouseRepository;
    private final OrderCustomerRepository orderCustomerRepository;

    @Transactional
    public CheckoutResponse createOrder(CheckoutRequest request) {

        CustomerType customerType;

        // Get customer if logged in
        CustomUserDetails customUserDetails = customerService.getCurrentUser();
        CustomerEntity customer = null;
        if (customUserDetails != null) {
            customerType = CustomerType.REGISTERED;
            customer = customerRepository.findById(customUserDetails.getId()).orElse(null);
        } else {
            customerType = CustomerType.GUEST;
        }

        OrderCustomerEntity orderCustomer = OrderCustomerEntity.builder()
                .customerType(customerType)
                .customer(customer)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        // Validate and get books
        List<AbstractBookEntity> books = validateAndGetBooks(request.getItems());
        List<String> physicalItems = request.getItems()
                .stream()
                .filter(i -> {
                    BookType bookType = BookType.valueOf(i.getItemType().toUpperCase());
                    return bookType == BookType.PHYSICAL;
                })
                .map(CheckoutItemDTO::getBookCode).toList();
        List<AbstractBookEntity> physicalBooks = books
                .stream()
                .filter(book -> physicalItems.contains(book.getCode()))
                .toList();
        
        // Validate stock for physical items
        validateStock(request.getItems(), books);
        
        // Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(request.getItems(), books);
        
        // Calculate delivery fee (if physical items)
        BigDecimal shippingAmount = BigDecimal.ZERO;
        DeliveryInfoEntity deliveryInfo = null;
        if (hasPhysicalItems(request.getItems())) {
            try {
                shippingAmount = calculateDeliveryFee(request, physicalBooks);
                deliveryInfo = createDeliveryInfo(request, shippingAmount);
            } catch (GhnApiClient.GhnApiException e) {
                log.warn("GHN API failed, proceeding without delivery fee: {}", e.getMessage());
                // Continue without delivery info for backward compatibility
            }
        }

        //TODO: Process ebook checkout
        
        // Calculate total
        BigDecimal totalAmount = subtotal.add(shippingAmount);
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setOrderCustomer(orderCustomer);
        order.setOrderType(determineOrderType(request.getItems()));
        order.setSubtotal(subtotal);
        order.setShippingAmount(shippingAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD); // TODO: Payment method
        order.setShippingAddress(serializeShippingAddress(request.getShippingAddress()));

        orderCustomer.setOrder(order);

        order = orderRepository.save(order);
        orderCustomerRepository.save(orderCustomer);
        
        // Create order entries and reserve inventory
        List<OrderEntryEntity> entries = createOrderEntries(order, request.getItems(), books);
        orderEntryRepository.saveAll(entries);
        order.setEntries(entries);
        
        // Reserve inventory for physical items
        reserveInventory(request.getItems(), books);
        
        // Save delivery info if available
        if (deliveryInfo != null) {
            deliveryInfo.setOrder(order);
            deliveryInfoRepository.save(deliveryInfo);
        }
        
        log.info("Order created successfully: {}", orderNumber);

        // Immediate confirm for COD
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            orderConfirmationService.autoConfirmOrder(order.getId());
        }
        
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(orderNumber);
        response.setTotalAmount(totalAmount);
        response.setStatus(order.getStatus().name());
        return response;
    }
    
    private List<AbstractBookEntity> validateAndGetBooks(List<CheckoutItemDTO> items) {
        // Support both bookId and bookCode
        // Map items to books maintaining order
        List<AbstractBookEntity> books = new java.util.ArrayList<>();
        
        for (CheckoutItemDTO item : items) {
            AbstractBookEntity book = null;
            
            if (item.getBookCode() != null && !item.getBookCode().isEmpty()) {
                book = abstractBookRepository.findByCode(item.getBookCode());
            } else if (item.getBookId() != null) {
                book = abstractBookRepository.findById(item.getBookId())
                    .orElse(null);
            }
            
            if (book == null) {
                throw new IllegalArgumentException("Book not found for item: " + 
                    (item.getBookCode() != null ? item.getBookCode() : "ID: " + item.getBookId()));
            }
            
            books.add(book);
        }
        
        return books;
    }
    
    private void validateStock(List<CheckoutItemDTO> items,
                               List<AbstractBookEntity> books) {
        for (int i = 0; i < items.size(); i++) {
            CheckoutItemDTO item = items.get(i);
            AbstractBookEntity abstractBook = books.get(i);
            
            if ("PHYSICAL".equals(item.getItemType()) && abstractBook.getPhysicalBookInfo() != null) {
                // For physical items, we need to check stock
                // For now, we'll check stock in the first available warehouse
                // In a real scenario, you'd determine which warehouse to use based on delivery address
                City deliveryCity = determineDeliveryCity();
                WarehouseEntity warehouse = warehouseRepository.findByCity(deliveryCity).get(0);

                StockLevel stockLevel = inventoryService.findStockLevelByBookIdAndWarehouse(abstractBook.getId(), warehouse.getId());
                log.info("Book {} - Available Qty: {} - Reserved Qty: {}", abstractBook.getTitle(), stockLevel.getAvailableQuantity(), item.getQuantity());

                if (stockLevel.getAvailableQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException(
                        String.format("Insufficient stock for book %s. Available: %d, Requested: %d",
                            abstractBook.getTitle(), stockLevel.getAvailableQuantity(), item.getQuantity()));
                }
            }
        }
    }
    
    private City determineDeliveryCity() {
        // Simplified - in real scenario, map GHN province/district to City enum
        // For now, default to HANOI
        return City.HANOI;
    }
    
    private BigDecimal calculateSubtotal(List<CheckoutItemDTO> items,
                                        List<AbstractBookEntity> books) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (int i = 0; i < items.size(); i++) {
            CheckoutItemDTO item = items.get(i);
            AbstractBookEntity abstractBook = books.get(i);

            BigDecimal unitPrice = BigDecimal.valueOf(0);
            if (item.getItemType().equals("PHYSICAL")) {
                unitPrice = abstractBook.getPhysicalBookInfo().getCurrentPrice();
            } else if (item.getItemType().equals("DIGITAL")) {
                unitPrice = abstractBook.getEbookInfo().getCurrentPrice();
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }
    
    private boolean hasPhysicalItems(List<CheckoutItemDTO> items) {
        return items.stream()
            .anyMatch(item -> "PHYSICAL".equals(item.getItemType()));
    }
    
    private BigDecimal calculateDeliveryFee(CheckoutRequest request, List<AbstractBookEntity> books) {
        // Simplified fee calculation - in real scenario, use GHN API with proper weight/dimensions
        CalculateFeeResponseDTO calculateFeeResponseDTO = deliveryService.calculateEstimatedDeliveryFee("2",
                request.getShippingAddress().getDistrictId().toString(),
                request.getShippingAddress().getWardCode(),
                books.stream().mapToInt(book -> book.getPhysicalBookInfo().getWeightGrams()).sum());
        return calculateFeeResponseDTO.getTotal();
    }
    
    private DeliveryInfoEntity createDeliveryInfo(CheckoutRequest request, BigDecimal shippingAmount) {
        return DeliveryInfoEntity.builder()
            .provinceId(request.getShippingAddress().getProvinceId())
            .districtId(request.getShippingAddress().getDistrictId())
            .wardCode(request.getShippingAddress().getWardCode())
            .serviceTypeId(2) // Standard
            .serviceId(53320)
            .weight(1000) // Default
            .length(20)
            .width(15)
            .height(5)
            .build();
    }
    
    private OrderType determineOrderType(List<CheckoutItemDTO> items) {
        boolean hasPhysical = items.stream().anyMatch(item -> "PHYSICAL".equals(item.getItemType()));
        boolean hasDigital = items.stream().anyMatch(item -> "DIGITAL".equals(item.getItemType()));
        
        if (hasPhysical && hasDigital) {
            return OrderType.MIXED;
        } else if (hasPhysical) {
            return OrderType.PHYSICAL;
        } else {
            return OrderType.DIGITAL;
        }
    }
    
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + random;
    }
    
    private String serializeShippingAddress(ShippingAddressDTO address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (Exception e) {
            log.error("Failed to serialize shipping address", e);
            return "{}";
        }
    }
    
    private List<OrderEntryEntity> createOrderEntries(OrderEntity order,
                                                     List<CheckoutItemDTO> items,
                                                     List<AbstractBookEntity> books) {
        List<OrderEntryEntity> entries = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i++) {
            CheckoutItemDTO item = items.get(i);
            AbstractBookEntity abstractBook = books.get(i);

            BigDecimal unitPrice = BigDecimal.valueOf(0);
            if (item.getItemType().equals("PHYSICAL")) {
                unitPrice = abstractBook.getPhysicalBookInfo().getCurrentPrice();
            } else if (item.getItemType().equals("DIGITAL")) {
                unitPrice = abstractBook.getEbookInfo().getCurrentPrice();
            }
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            OrderEntryEntity entry = new OrderEntryEntity();
            entry.setOrder(order);
            entry.setBook(abstractBook);
            entry.setItemType(ItemType.valueOf(item.getItemType()));
            entry.setQuantity(item.getQuantity());
            entry.setUnitPrice(unitPrice);
            entry.setTotalPrice(totalPrice);
            
            entries.add(entry);
        }
        
        return entries;
    }
    
    private void reserveInventory(List<CheckoutItemDTO> items,
                                 List<AbstractBookEntity> books) {
        City deliveryCity = determineDeliveryCity();
        WarehouseEntity warehouse = warehouseRepository.findByCity(deliveryCity).get(0);
        
        for (int i = 0; i < items.size(); i++) {
            CheckoutItemDTO item = items.get(i);
            AbstractBookEntity abstractBook = books.get(i);
            
            if ("PHYSICAL".equals(item.getItemType()) && abstractBook.getPhysicalBookInfo() != null) {
                    inventoryService.reserveBookInventory(abstractBook.getId(), warehouse.getId(), item.getQuantity());
                }
            }
        }
}

