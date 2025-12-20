package com.huongcung.storefront.checkout.service.impl;

import com.huongcung.core.catalog.enumeration.BookType;
import com.huongcung.core.inventory.service.InventoryService;
import com.huongcung.core.catalog.model.entity.AbstractBookEntity;
import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.inventory.model.domain.StockLevel;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.logistics.external.ghn.exception.GhnApiException;
import com.huongcung.core.logistics.model.dto.*;
import com.huongcung.core.logistics.model.dto.request.CalculateFeeRequest;
import com.huongcung.core.order.enumeration.*;
import com.huongcung.core.order.model.dto.AllocationPlanDTO;
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
import com.huongcung.core.order.strategy.SplitOrderStrategy;
import com.huongcung.core.security.model.dto.CustomUserDetails;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.repository.CustomerRepository;
import com.huongcung.core.user.service.CustomerService;
import com.huongcung.storefront.checkout.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.storefront.checkout.service.CheckoutService;
import com.huongcung.core.logistics.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final SplitOrderStrategy splitOrderStrategy;

    @Override
    public EstimatedDeliveryInfoResponse calculateEstimatedDeliveryFee(CalculateFeeRequest request) {
        AddressDTO customerAddress = new AddressDTO();
        populateAddress(request.getShippingAddress(), customerAddress);
        List<CheckoutItemDTO> physicalItems = request.getItems().stream()
                .filter(i -> i.getBookType() == BookType.PHYSICAL)
                .toList();

        // Resolve books from items (by bookCode or bookId as before)
        List<AbstractBookEntity> books = validateAndGetBooks(physicalItems);

        // Build map by book code to avoid relying on numeric IDs from frontend
        Map<String, AbstractBookEntity> code2BookMap = books.stream()
                .collect(Collectors.toMap(
                        AbstractBookEntity::getCode,
                        Function.identity()
                ));

        // Map each CheckoutItemDTO to its corresponding book
        Map<CheckoutItemDTO, AbstractBookEntity> item2BookMap = physicalItems.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        item -> {
                            AbstractBookEntity book = code2BookMap.get(item.getBookCode());
                            if (book == null) {
                                throw new IllegalArgumentException(
                                        "Book not found for item when calculating fee: " +
                                                (item.getBookCode() != null ? item.getBookCode() : "null code"));
                            }
                            return book;
                        }
                ));

        List<OrderEntryEntity> orderEntries = createOrderEntries(null, item2BookMap);
        AllocationPlanDTO allocationPlan = splitOrderStrategy.simulateSplitOrder(orderEntries, customerAddress);

        // Convert expected delivery time (number of days) to a concrete LocalDate
        int expectedDays = allocationPlan.getExpectedDeliveryTime();
        LocalDate expectedDeliveryDate = LocalDate.now().plusDays(expectedDays);

        return EstimatedDeliveryInfoResponse.builder()
                .warehouseCount(allocationPlan.getWarehouseCount())
                .expectedDeliveryTime(expectedDeliveryDate)
                .totalFee(allocationPlan.getTotalFee())
                .build();
    }

    private void populateAddress(ShippingAddressDTO source, AddressDTO target) {
        target.setAddress(source.getAddress());
        WardDTO ward = WardDTO.builder()
                .wardCode(source.getWardCode())
                .wardName(source.getWardName())
                .build();
        target.setWard(ward);
        DistrictDTO district = DistrictDTO.builder()
                .districtName(source.getDistrictName())
                .districtId(source.getDistrictId())
                .build();
        target.setDistrict(district);
        ProvinceDTO province = ProvinceDTO.builder()
                .provinceId(source.getProvinceId())
                .provinceName(source.getProvinceName())
                .build();
        target.setProvince(province);
        target.setServiceTypeId(source.getServiceTypeId());
    }

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
                .filter(i -> i.getBookType() == BookType.PHYSICAL)
                .map(CheckoutItemDTO::getBookCode)
                .toList();
        List<AbstractBookEntity> physicalBooks = books
                .stream()
                .filter(book -> physicalItems.contains(book.getCode()))
                .toList();

        // Validate stock for physical items
        validateStock(request.getItems(), physicalBooks);
        
        // Calculate subtotal
        BigDecimal subtotal = calculateSubtotal(request.getItems(), books);
        
        // Calculate delivery fee (if physical items)
        BigDecimal shippingAmount = calculateDeliveryFee(request);

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
        // Set payment method from request, default to COD if not provided
        PaymentMethod paymentMethod = request.getPaymentMethod() != null 
            ? request.getPaymentMethod() 
            : PaymentMethod.COD;
        order.setPaymentMethod(paymentMethod);
        order.setShippingAddress(serializeShippingAddress(request.getShippingAddress()));

        orderCustomer.setOrder(order);

        order = orderRepository.save(order);
        orderCustomerRepository.save(orderCustomer);

        // Create order entries based on resolved books
        Map<String, AbstractBookEntity> code2BookMap = books.stream()
                .collect(Collectors.toMap(
                        AbstractBookEntity::getCode,
                        Function.identity()
                ));
        Map<CheckoutItemDTO, AbstractBookEntity> item2BookMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        item -> {
                            AbstractBookEntity book = code2BookMap.get(item.getBookCode());
                            if (book == null) {
                                throw new IllegalArgumentException(
                                        "Book not found for item when creating order: " +
                                                (item.getBookCode() != null ? item.getBookCode() : "null code"));
                            }
                            return book;
                        }
                ));

        List<OrderEntryEntity> entries = createOrderEntries(order, item2BookMap);
        orderEntryRepository.saveAll(entries);
        order.setEntries(entries);
        
        log.info("Order created successfully: {}", orderNumber);

        // Immediate confirm for COD only
        // VNPay orders will be confirmed after successful payment via IPN callback
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

        List<AbstractBookEntity> books = new ArrayList<>();
        
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

            if (item.getBookType() == BookType.PHYSICAL && abstractBook.getPhysicalBookInfo() != null) {
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

        return City.HANOI;
    }
    
    private BigDecimal calculateSubtotal(List<CheckoutItemDTO> items,
                                        List<AbstractBookEntity> books) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (int i = 0; i < items.size(); i++) {
            CheckoutItemDTO item = items.get(i);
            AbstractBookEntity abstractBook = books.get(i);

            BigDecimal unitPrice = BigDecimal.ZERO;
            if (item.getBookType() == BookType.PHYSICAL) {
                unitPrice = abstractBook.getPhysicalBookInfo().getCurrentPrice();
            } else if (item.getBookType() == BookType.EBOOK) {
                unitPrice = abstractBook.getEbookInfo().getCurrentPrice();
            }

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        return subtotal;
    }
    
    private boolean hasPhysicalItems(List<CheckoutItemDTO> items) {
        return items.stream()
                .anyMatch(item -> item.getBookType() == BookType.PHYSICAL);
    }
    
    private BigDecimal calculateDeliveryFee(CheckoutRequest request) {
        CalculateFeeRequest calculateFeeRequest = CalculateFeeRequest.builder()
                .items(request.getItems())
                .shippingAddress(request.getShippingAddress())
                .build();
        EstimatedDeliveryInfoResponse estimatedDeliveryInfo = calculateEstimatedDeliveryFee(calculateFeeRequest);
        return estimatedDeliveryInfo.getTotalFee();
    }
    
    private OrderType determineOrderType(List<CheckoutItemDTO> items) {
        boolean hasPhysical = items.stream().anyMatch(item -> item.getBookType() == BookType.PHYSICAL);
        boolean hasDigital = items.stream().anyMatch(item -> item.getBookType() == BookType.EBOOK);

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
                                                      Map<CheckoutItemDTO, AbstractBookEntity> item2BookMap) {

        List<OrderEntryEntity> entries = new ArrayList<>();

        for (Map.Entry<CheckoutItemDTO, AbstractBookEntity> entryMap : item2BookMap.entrySet()) {
            CheckoutItemDTO item = entryMap.getKey();
            AbstractBookEntity abstractBook = entryMap.getValue();

            BigDecimal unitPrice = BigDecimal.ZERO;
            if (item.getBookType() == BookType.PHYSICAL) {
                unitPrice = abstractBook.getPhysicalBookInfo().getCurrentPrice();
            } else if (item.getBookType() == BookType.EBOOK) {
                unitPrice = abstractBook.getEbookInfo().getCurrentPrice();
            }

            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderEntryEntity entry = new OrderEntryEntity();
            entry.setOrder(order);
            entry.setBook(abstractBook);
            ItemType itemType = (item.getBookType() == BookType.PHYSICAL) ? ItemType.PHYSICAL : ItemType.DIGITAL;
            entry.setItemType(itemType);
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

            if (item.getBookType() == BookType.PHYSICAL && abstractBook.getPhysicalBookInfo() != null) {
                inventoryService.reserveBookInventory(abstractBook.getId(), warehouse.getId(), item.getQuantity());
            }
        }
    }
}

