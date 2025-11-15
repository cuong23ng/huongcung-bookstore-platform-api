package com.huongcung.webstore.checkout.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.inventory.enumeration.City;
import com.huongcung.core.inventory.model.entity.StockLevelEntity;
import com.huongcung.core.inventory.model.entity.WarehouseEntity;
import com.huongcung.core.inventory.repository.StockLevelRepository;
import com.huongcung.core.inventory.repository.WarehouseRepository;
import com.huongcung.core.order.enumeration.OrderStatus;
import com.huongcung.core.order.enumeration.OrderType;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.enumeration.PaymentStatus;
import com.huongcung.core.order.model.entity.OrderEntity;
import com.huongcung.core.order.repository.DeliveryInfoRepository;
import com.huongcung.core.order.repository.OrderEntryRepository;
import com.huongcung.core.order.repository.OrderRepository;
import com.huongcung.core.product.model.entity.EbookEntity;
import com.huongcung.core.product.model.entity.PhysicalBookEntity;
import com.huongcung.core.product.repository.AbstractBookRepository;
import com.huongcung.core.user.model.entity.CustomerEntity;
import com.huongcung.core.user.repository.UserRepository;
import com.huongcung.webstore.checkout.dto.CheckoutItemDTO;
import com.huongcung.webstore.checkout.dto.CheckoutRequest;
import com.huongcung.webstore.checkout.dto.CheckoutResponse;
import com.huongcung.webstore.checkout.dto.ShippingAddressDTO;
import com.huongcung.webstore.checkout.external.ghn.GhnApiClient;
import com.huongcung.webstore.checkout.external.ghn.dto.CalculateFeeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutService Unit Tests")
class CheckoutServiceTest {
    
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
    private EbookEntity testEbook;
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
        
        // Setup test ebook
        testEbook = new EbookEntity();
        testEbook.setId(2L);
        testEbook.setCode("EBOOK001");
        testEbook.setTitle("Test Ebook");
        testEbook.setCurrentPrice(new BigDecimal("100000"));
        
        // Setup test warehouse
        testWarehouse = new WarehouseEntity();
        testWarehouse.setId(1L);
        testWarehouse.setCity(City.HANOI);
        testWarehouse.setCode("WH-HN-001");
        
        // Setup test stock level
        testStockLevel = new StockLevelEntity();
        testStockLevel.setId(1L);
        testStockLevel.setBook(testPhysicalBook);
        testStockLevel.setWarehouse(testWarehouse);
        testStockLevel.setQuantity(100);
        testStockLevel.setReservedQuantity(0);
    }
    
    @Test
    @DisplayName("Should successfully create order with physical items")
    void createOrder_WithPhysicalItems_Success() throws Exception {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(1L);
        item.setQuantity(2);
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
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testPhysicalBook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        when(stockLevelRepository.findByBookAndCityWithLock(eq(testPhysicalBook), eq(City.HANOI)))
            .thenReturn(Optional.of(testStockLevel));
        
        CalculateFeeResponse feeResponse = new CalculateFeeResponse();
        feeResponse.setTotal(new BigDecimal("30000"));
        when(ghnApiClient.calculateFee(any())).thenReturn(feeResponse);
        
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-20241201120000-ABC123");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When
        CheckoutResponse response = checkoutService.createOrder(request, 1L);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertNotNull(response.getOrderNumber());
        assertEquals(OrderStatus.PENDING.name(), response.getStatus());
        
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(orderEntryRepository, times(1)).saveAll(anyList());
        verify(stockLevelRepository, times(1)).save(any(StockLevelEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when customer not found")
    void createOrder_CustomerNotFound_ThrowsException() {
        // Given
        CheckoutRequest request = new CheckoutRequest();
        request.setItems(Collections.emptyList());
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.createOrder(request, 1L);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when book not found")
    void createOrder_BookNotFound_ThrowsException() {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(999L);
        item.setQuantity(1);
        item.setItemType("PHYSICAL");
        
        CheckoutRequest request = new CheckoutRequest();
        request.setItems(Collections.singletonList(item));
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.emptyList());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.createOrder(request, 1L);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(1L);
        item.setQuantity(150); // More than available (100)
        item.setItemType("PHYSICAL");
        
        ShippingAddressDTO address = ShippingAddressDTO.builder()
            .provinceId(201)
            .districtId(1442)
            .wardCode("1A0001")
            .build();
        
        CheckoutRequest request = new CheckoutRequest();
        request.setItems(Collections.singletonList(item));
        request.setShippingAddress(address);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testPhysicalBook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            checkoutService.createOrder(request, 1L);
        });
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }
    
    @Test
    @DisplayName("Should create order with digital items without stock check")
    void createOrder_WithDigitalItems_Success() throws Exception {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(2L);
        item.setQuantity(1);
        item.setItemType("DIGITAL");
        
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
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testEbook));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-20241201120000-ABC123");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When
        CheckoutResponse response = checkoutService.createOrder(request, 1L);
        
        // Then
        assertNotNull(response);
        verify(stockLevelRepository, never()).findByBookIdAndWarehouseCity(any(), any());
        verify(stockLevelRepository, never()).save(any(StockLevelEntity.class));
    }
    
    @Test
    @DisplayName("Should handle GHN API failure gracefully")
    void createOrder_GhnApiFailure_ProceedsWithoutDeliveryInfo() throws Exception {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(1L);
        item.setQuantity(2);
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
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testPhysicalBook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        when(stockLevelRepository.findByBookAndCityWithLock(eq(testPhysicalBook), eq(City.HANOI)))
            .thenReturn(Optional.of(testStockLevel));
        
        when(ghnApiClient.calculateFee(any())).thenThrow(new GhnApiClient.GhnApiException("GHN API unavailable"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-20241201120000-ABC123");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When
        CheckoutResponse response = checkoutService.createOrder(request, 1L);
        
        // Then
        assertNotNull(response);
        // Order should be created even without delivery info
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
        verify(deliveryInfoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should reserve inventory when order created")
    void createOrder_ReservesInventory() throws Exception {
        // Given
        CheckoutItemDTO item = new CheckoutItemDTO();
        item.setBookId(1L);
        item.setQuantity(5);
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
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Collections.singletonList(testPhysicalBook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        when(stockLevelRepository.findByBookAndCityWithLock(eq(testPhysicalBook), eq(City.HANOI)))
            .thenReturn(Optional.of(testStockLevel));
        
        when(ghnApiClient.calculateFee(any())).thenThrow(new GhnApiClient.GhnApiException("GHN API unavailable"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-20241201120000-ABC123");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When
        checkoutService.createOrder(request, 1L);
        
        // Then
        ArgumentCaptor<StockLevelEntity> stockCaptor = ArgumentCaptor.forClass(StockLevelEntity.class);
        verify(stockLevelRepository, times(1)).save(stockCaptor.capture());
        
        StockLevelEntity savedStock = stockCaptor.getValue();
        assertEquals(5, savedStock.getReservedQuantity()); // Should be reserved
    }
    
    @Test
    @DisplayName("Should determine order type correctly - MIXED")
    void createOrder_MixedOrderType() throws Exception {
        // Given
        CheckoutItemDTO physicalItem = new CheckoutItemDTO();
        physicalItem.setBookId(1L);
        physicalItem.setQuantity(1);
        physicalItem.setItemType("PHYSICAL");
        
        CheckoutItemDTO digitalItem = new CheckoutItemDTO();
        digitalItem.setBookId(2L);
        digitalItem.setQuantity(1);
        digitalItem.setItemType("DIGITAL");
        
        ShippingAddressDTO address = ShippingAddressDTO.builder()
            .fullName("Test User")
            .phone("0123456789")
            .address("123 Test Street")
            .provinceId(201)
            .districtId(1442)
            .wardCode("1A0001")
            .build();
        
        CheckoutRequest request = new CheckoutRequest();
        request.setItems(Arrays.asList(physicalItem, digitalItem));
        request.setShippingAddress(address);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findByIdIn(anyList())).thenReturn(Arrays.asList(testPhysicalBook, testEbook));
        when(warehouseRepository.findByCity(City.HANOI)).thenReturn(Collections.singletonList(testWarehouse));
        when(stockLevelRepository.findByBookIdAndWarehouseCity(1L, City.HANOI))
            .thenReturn(Optional.of(testStockLevel));
        when(stockLevelRepository.findByBookAndCityWithLock(eq(testPhysicalBook), eq(City.HANOI)))
            .thenReturn(Optional.of(testStockLevel));
        
        when(ghnApiClient.calculateFee(any())).thenThrow(new GhnApiClient.GhnApiException("GHN API unavailable"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        OrderEntity savedOrder = new OrderEntity();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("ORD-20241201120000-ABC123");
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        
        // When
        checkoutService.createOrder(request, 1L);
        
        // Then
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        
        OrderEntity savedOrderEntity = orderCaptor.getValue();
        assertEquals(OrderType.MIXED, savedOrderEntity.getOrderType());
        assertEquals(PaymentMethod.COD, savedOrderEntity.getPaymentMethod());
        assertEquals(OrderStatus.PENDING, savedOrderEntity.getStatus());
        assertEquals(PaymentStatus.PENDING, savedOrderEntity.getPaymentStatus());
    }
}

