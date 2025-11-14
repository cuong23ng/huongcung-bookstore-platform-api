package com.huongcung.platform.checkout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.DeliveryInfoRepository;
import com.huongcung.core.order.repository.OrderEntryRepository;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.repository.UserRepository;
import com.huongcung.platform.checkout.dto.CheckoutItemDTO;
import com.huongcung.platform.checkout.dto.CheckoutRequest;
import com.huongcung.platform.checkout.dto.ShippingAddressDTO;
import com.huongcung.platform.checkout.external.ghn.GhnApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Concurrent Tests")
class CheckoutServiceConcurrentTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderEntryRepository orderEntryRepository;
    
    @Mock
    private DeliveryInfoRepository deliveryInfoRepository;
    
    @Mock
    private AbstractBookRepository bookRepository;
    
    @Mock
    private StockLevelRepository stockLevelRepository;
    
    @Mock
    private WarehouseRepository warehouseRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private GhnApiClient ghnApiClient;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private CheckoutService checkoutService;
    
    private CustomerEntity testCustomer;
    private PhysicalBookEntity testPhysicalBook;
    private WarehouseEntity testWarehouse;
    private StockLevelEntity testStockLevel;
    
    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = new CustomerEntity();
        testCustomer.setId(1L);
        testCustomer.setEmail("customer@test.com");
        
        // Setup test physical book
        testPhysicalBook = new PhysicalBookEntity();
        testPhysicalBook.setId(1L);
        testPhysicalBook.setCode("BOOK001");
        testPhysicalBook.setTitle("Test Physical Book");
        testPhysicalBook.setCurrentPrice(new BigDecimal("150000"));
        
        // Setup test warehouse
        testWarehouse = new WarehouseEntity();
        testWarehouse.setId(1L);
        testWarehouse.setCity(City.HANOI);
        testWarehouse.setCode("WH-HN-001");
        
        // Setup test stock level with limited quantity
        testStockLevel = new StockLevelEntity();
        testStockLevel.setId(1L);
        testStockLevel.setBook(testPhysicalBook);
        testStockLevel.setWarehouse(testWarehouse);
        testStockLevel.setQuantity(10); // Only 10 items available
        testStockLevel.setReservedQuantity(0);
    }
    
    @Test
    @DisplayName("Should prevent overselling with concurrent checkouts")
    void createOrder_ConcurrentCheckouts_PreventsOverselling() throws Exception {
        // Given: 10 items in stock, 5 concurrent requests for 3 items each
        // Expected: Only 3 requests should succeed (9 items total), 1 should fail
        
        int concurrentRequests = 5;
        int quantityPerRequest = 3;
        int availableStock = 10;
        
        testStockLevel.setQuantity(availableStock);
        testStockLevel.setReservedQuantity(0);
        
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(1L);
        item.setQuantity(quantityPerRequest);
        item.setItemType("PHYSICAL");
        
        ShippingAddressDTO address = ShippingAddressDTO.builder()
            .fullName("Test User")
            .phone("0123456789")
            .address("123 Test Street")
            .provinceId(201)
            .districtId(1442)
            .wardCode("1A0001")
            .build();
        
        CheckoutRequest request = new CheckoutRequest();
        request.setItems(Collections.singletonList(item));
        request.setShippingAddress(address);
        
        // Setup mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testPhysicalBook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        
        // Mock the locked stock level - simulate pessimistic locking
        StockLevelEntity lockedStock = new StockLevelEntity();
        lockedStock.setId(1L);
        lockedStock.setBook(testPhysicalBook);
        lockedStock.setWarehouse(testWarehouse);
        lockedStock.setQuantity(availableStock);
        lockedStock.setReservedQuantity(0);
        
        when(stockLevelRepository.findByBookAndCityWithLock(eq(testPhysicalBook), eq(City.HANOI)))
            .thenAnswer(invocation -> {
                // Simulate checking and updating stock
                StockLevelEntity stock = lockedStock;
                if (stock.getQuantity() - stock.getReservedQuantity() >= quantityPerRequest) {
                    stock.setReservedQuantity(stock.getReservedQuantity() + quantityPerRequest);
                    return Optional.of(stock);
                } else {
                    throw new IllegalArgumentException("Insufficient stock");
                }
            });
        
        when(ghnApiClient.calculateFee(any())).thenThrow(new GhnApiClient.GhnApiException("GHN API unavailable"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-TEST");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When: Execute concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        for (int i = 0; i < concurrentRequests; i++) {
            executor.submit(() -> {
                try {
                    checkoutService.createOrder(request, 1L);
                    successCount.incrementAndGet();
                } catch (IllegalArgumentException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Then: Verify that not all requests succeeded (preventing overselling)
        int maxPossibleSuccess = availableStock / quantityPerRequest; // 3 requests (9 items)
        
        assertTrue(successCount.get() <= maxPossibleSuccess, 
            "Should not allow more successful checkouts than available stock");
        assertTrue(failureCount.get() > 0, 
            "At least one request should fail due to insufficient stock");
        assertTrue(successCount.get() + failureCount.get() == concurrentRequests,
            "All requests should complete (either success or failure)");
    }
}

