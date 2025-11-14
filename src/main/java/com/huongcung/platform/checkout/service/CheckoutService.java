package com.huongcung.platform.checkout.service;

import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.order.enumeration.ItemType;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.OrderType;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.model.entity.DeliveryInfoEntity;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.core.order.repository.DeliveryInfoRepository;
import com.huongcung.core.order.repository.OrderEntryRepository;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.product.model.entity.AbstractBookEntity;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.user.model.entity.UserEntity;
import com.huongcung.core.user.repository.UserRepository;
import com.huongcung.platform.checkout.dto.CheckoutRequest;
import com.huongcung.platform.checkout.dto.CheckoutResponse;
import com.huongcung.platform.checkout.external.ghn.GhnApiClient;
import com.huongcung.platform.checkout.external.ghn.dto.CalculateFeeRequest;
import com.huongcung.platform.checkout.external.ghn.dto.CalculateFeeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {
    
    private final OrderRepository orderRepository;
    private final OrderEntryRepository orderEntryRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final AbstractBookRepository bookRepository;
    private final StockLevelRepository stockLevelRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final GhnApiClient ghnApiClient;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public CheckoutResponse createOrder(CheckoutRequest request, Long customerId) {
        log.info("Creating order for customer: {}", customerId);
        
        // Get customer
        UserEntity customer = userRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        
        // Validate and get books
        List<AbstractBookEntity> books = validateAndGetBooks(request.getItems());
        
        // Validate stock for physical items
        validateStock(request.getItems(), books);
        
        // Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(request.getItems(), books);
        
        // Calculate delivery fee (if physical items)
        BigDecimal shippingAmount = BigDecimal.ZERO;
        DeliveryInfoEntity deliveryInfo = null;
        if (hasPhysicalItems(request.getItems())) {
            try {
                shippingAmount = calculateDeliveryFee(request);
                deliveryInfo = createDeliveryInfo(request, shippingAmount);
            } catch (GhnApiClient.GhnApiException e) {
                log.warn("GHN API failed, proceeding without delivery fee: {}", e.getMessage());
                // Continue without delivery info for backward compatibility
            }
        }
        
        // Calculate total
        BigDecimal totalAmount = subtotal.add(shippingAmount);
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setOrderType(determineOrderType(request.getItems()));
        order.setSubtotal(subtotal);
        order.setShippingAmount(shippingAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setShippingAddress(serializeShippingAddress(request.getShippingAddress()));
        
        order = orderRepository.save(order);
        
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
        
        CheckoutResponse response = new CheckoutResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(orderNumber);
        response.setTotalAmount(totalAmount);
        response.setStatus(order.getStatus().name());
        return response;
    }
    
    private List<AbstractBookEntity> validateAndGetBooks(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items) {
        // Support both bookId and bookCode
        // Map items to books maintaining order
        List<AbstractBookEntity> books = new java.util.ArrayList<>();
        
        for (com.huongcung.platform.checkout.dto.CheckoutItemDTO item : items) {
            AbstractBookEntity book = null;
            
            if (item.getBookCode() != null && !item.getBookCode().isEmpty()) {
                book = bookRepository.findAbstractBookEntityByCode(item.getBookCode());
            } else if (item.getBookId() != null) {
                book = bookRepository.findById(item.getBookId())
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
    
    private void validateStock(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items, 
                               List<AbstractBookEntity> books) {
        for (int i = 0; i < items.size(); i++) {
            com.huongcung.platform.checkout.dto.CheckoutItemDTO item = items.get(i);
            AbstractBookEntity book = books.get(i);
            
            if ("PHYSICAL".equals(item.getItemType()) && book instanceof PhysicalBookEntity) {
                // For physical items, we need to check stock
                // For now, we'll check stock in the first available warehouse
                // In a real scenario, you'd determine which warehouse to use based on delivery address
                City deliveryCity = determineDeliveryCity(); // Simplified - should use GHN address
                
                WarehouseEntity warehouse = warehouseRepository.findByCity(deliveryCity)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No warehouse found for city: " + deliveryCity));
                
                StockLevelEntity stockLevel = stockLevelRepository
                    .findByBookIdAndWarehouseCity(book.getId(), deliveryCity)
                    .orElseThrow(() -> new IllegalStateException(
                        "Stock level not found for book: " + book.getId() + " in city: " + deliveryCity));
                
                int availableQuantity = stockLevel.getQuantity() - stockLevel.getReservedQuantity();
                if (availableQuantity < item.getQuantity()) {
                    throw new IllegalArgumentException(
                        String.format("Insufficient stock for book %s. Available: %d, Requested: %d",
                            book.getTitle(), availableQuantity, item.getQuantity()));
                }
            }
        }
    }
    
    private City determineDeliveryCity() {
        // Simplified - in real scenario, map GHN province/district to City enum
        // For now, default to HANOI
        return City.HANOI;
    }
    
    private BigDecimal calculateSubtotal(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items,
                                        List<AbstractBookEntity> books) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (int i = 0; i < items.size(); i++) {
            com.huongcung.platform.checkout.dto.CheckoutItemDTO item = items.get(i);
            AbstractBookEntity book = books.get(i);

            BigDecimal unitPrice = BigDecimal.valueOf(0);
            if (book instanceof PhysicalBookEntity) {
                unitPrice = ((PhysicalBookEntity) book).getCurrentPrice();
            } else if (book instanceof EbookEntity) {
                unitPrice = ((EbookEntity) book).getCurrentPrice();
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }
    
    private boolean hasPhysicalItems(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items) {
        return items.stream()
            .anyMatch(item -> "PHYSICAL".equals(item.getItemType()));
    }
    
    private BigDecimal calculateDeliveryFee(CheckoutRequest request) {
        // Simplified fee calculation - in real scenario, use GHN API with proper weight/dimensions
        CalculateFeeRequest ghnRequest = CalculateFeeRequest.builder()
            .serviceTypeId(2) // Standard delivery
            .serviceId(53320) // Standard service
            .toDistrictId(request.getShippingAddress().getDistrictId())
            .toWardCode(request.getShippingAddress().getWardCode())
            .weight(1000) // Default 1kg
            .length(20)
            .width(15)
            .height(5)
            .build();
        
        CalculateFeeResponse response = ghnApiClient.calculateFee(ghnRequest);
        return response.getTotal();
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
    
    private OrderType determineOrderType(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items) {
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
    
    private String serializeShippingAddress(com.huongcung.platform.checkout.dto.ShippingAddressDTO address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (Exception e) {
            log.error("Failed to serialize shipping address", e);
            return "{}";
        }
    }
    
    private List<OrderEntryEntity> createOrderEntries(OrderEntity order,
                                                     List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items,
                                                     List<AbstractBookEntity> books) {
        List<OrderEntryEntity> entries = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i++) {
            com.huongcung.platform.checkout.dto.CheckoutItemDTO item = items.get(i);
            AbstractBookEntity book = books.get(i);

            BigDecimal unitPrice = BigDecimal.valueOf(0);
            if (book instanceof PhysicalBookEntity) {
                unitPrice = ((PhysicalBookEntity) book).getCurrentPrice();
            } else if (book instanceof EbookEntity) {
                unitPrice = ((EbookEntity) book).getCurrentPrice();
            }
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            OrderEntryEntity entry = new OrderEntryEntity();
            entry.setOrder(order);
            entry.setBook(book);
            entry.setItemType(ItemType.valueOf(item.getItemType()));
            entry.setQuantity(item.getQuantity());
            entry.setUnitPrice(unitPrice);
            entry.setTotalPrice(totalPrice);
            
            entries.add(entry);
        }
        
        return entries;
    }
    
    private void reserveInventory(List<com.huongcung.platform.checkout.dto.CheckoutItemDTO> items,
                                 List<AbstractBookEntity> books) {
        City deliveryCity = determineDeliveryCity();
        
        for (int i = 0; i < items.size(); i++) {
            com.huongcung.platform.checkout.dto.CheckoutItemDTO item = items.get(i);
            AbstractBookEntity book = books.get(i);
            
            if ("PHYSICAL".equals(item.getItemType()) && book instanceof PhysicalBookEntity) {
                StockLevelEntity stockLevel = stockLevelRepository
                    .findByBookIdAndWarehouseCity(book.getId(), deliveryCity)
                    .orElse(null);
                
                if (stockLevel != null) {
                    // Use pessimistic lock to prevent race conditions
                    StockLevelEntity lockedStock = stockLevelRepository
                        .findByBookAndCityWithLock((PhysicalBookEntity) book, deliveryCity)
                        .orElse(stockLevel);
                    
                    int newReserved = lockedStock.getReservedQuantity() + item.getQuantity();
                    lockedStock.setReservedQuantity(newReserved);
                    stockLevelRepository.save(lockedStock);
                    
                    log.debug("Reserved {} units of book {} in city {}", 
                        item.getQuantity(), book.getId(), deliveryCity);
                }
            }
        }
    }
}

