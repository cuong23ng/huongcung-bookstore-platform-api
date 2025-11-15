package com.huongcung.webstore.checkout.external.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.webstore.checkout.external.ghn.config.GhnApiConfig;
import com.huongcung.webstore.checkout.external.ghn.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GhnApiClient Unit Tests")
class GhnApiClientTest {
    
    @Mock
    private GhnApiConfig ghnApiConfig;
    
    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private GhnApiClient ghnApiClient;
    
    @BeforeEach
    void setUp() {
        when(ghnApiConfig.getBaseUrl()).thenReturn("https://dev-online-gateway.ghn.vn");
        when(ghnApiConfig.getApiToken()).thenReturn("test-token");
        when(ghnApiConfig.getShopId()).thenReturn(12345);
    }
    
    @Test
    @DisplayName("Should successfully get provinces")
    void getProvinces_Success() {
        // Given
        GhnProvinceDTO province1 = new GhnProvinceDTO();
        province1.setProvinceId(201);
        province1.setProvinceName("Hà Nội");
        
        GhnProvinceDTO province2 = new GhnProvinceDTO();
        province2.setProvinceId(202);
        province2.setProvinceName("Hồ Chí Minh");
        
        List<GhnProvinceDTO> provinces = Arrays.asList(province1, province2);
        
        GhnApiResponse<List<GhnProvinceDTO>> response = new GhnApiResponse<>();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(provinces);
        
        ResponseEntity<GhnApiResponse<List<GhnProvinceDTO>>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When
        List<GhnProvinceDTO> result = ghnApiClient.getProvinces();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hà Nội", result.get(0).getProvinceName());
        verify(restTemplate, times(1)).exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        );
    }
    
    @Test
    @DisplayName("Should return empty list when GHN API fails")
    void getProvinces_ApiFailure_ReturnsEmptyList() {
        // Given
        GhnApiResponse<List<GhnProvinceDTO>> response = new GhnApiResponse<>();
        response.setCode(500);
        response.setMessage("Internal Server Error");
        response.setData(null);
        
        ResponseEntity<GhnApiResponse<List<GhnProvinceDTO>>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When
        List<GhnProvinceDTO> result = ghnApiClient.getProvinces();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("Should throw GhnApiException when RestClientException occurs")
    void getProvinces_RestClientException_ThrowsGhnApiException() {
        // Given
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Connection timeout"));
        
        // When & Then
        assertThrows(GhnApiClient.GhnApiException.class, () -> {
            ghnApiClient.getProvinces();
        });
    }
    
    @Test
    @DisplayName("Should successfully get districts for province")
    void getDistricts_Success() {
        // Given
        Integer provinceId = 201;
        
        GhnDistrictDTO district1 = new GhnDistrictDTO();
        district1.setDistrictId(1442);
        district1.setDistrictName("Quận Ba Đình");
        district1.setProvinceId(provinceId);
        
        List<GhnDistrictDTO> districts = Collections.singletonList(district1);
        
        GhnApiResponse<List<GhnDistrictDTO>> response = new GhnApiResponse<>();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(districts);
        
        ResponseEntity<GhnApiResponse<List<GhnDistrictDTO>>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When
        List<GhnDistrictDTO> result = ghnApiClient.getDistricts(provinceId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Quận Ba Đình", result.get(0).getDistrictName());
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when province ID is null")
    void getDistricts_NullProvinceId_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ghnApiClient.getDistricts(null);
        });
    }
    
    @Test
    @DisplayName("Should successfully get wards for district")
    void getWards_Success() {
        // Given
        Integer districtId = 1442;
        
        GhnWardDTO ward1 = new GhnWardDTO();
        ward1.setWardCode("1A0001");
        ward1.setWardName("Phường Cống Vị");
        ward1.setDistrictId(districtId);
        
        List<GhnWardDTO> wards = Collections.singletonList(ward1);
        
        GhnApiResponse<List<GhnWardDTO>> response = new GhnApiResponse<>();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(wards);
        
        ResponseEntity<GhnApiResponse<List<GhnWardDTO>>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When
        List<GhnWardDTO> result = ghnApiClient.getWards(districtId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Phường Cống Vị", result.get(0).getWardName());
    }
    
    @Test
    @DisplayName("Should successfully calculate delivery fee")
    void calculateFee_Success() {
        // Given
        CalculateFeeRequest request = CalculateFeeRequest.builder()
            .serviceTypeId(2)
            .serviceId(53320)
            .toDistrictId(1442)
            .toWardCode("1A0001")
            .weight(1000)
            .length(20)
            .width(15)
            .height(5)
            .build();
        
        CalculateFeeResponse feeResponse = new CalculateFeeResponse();
        feeResponse.setTotal(new BigDecimal("30000"));
        feeResponse.setServiceFee(new BigDecimal("25000"));
        feeResponse.setInsuranceFee(new BigDecimal("5000"));
        
        GhnApiResponse<CalculateFeeResponse> response = new GhnApiResponse<>();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(feeResponse);
        
        ResponseEntity<GhnApiResponse<CalculateFeeResponse>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When
        CalculateFeeResponse result = ghnApiClient.calculateFee(request);
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("30000"), result.getTotal());
        assertEquals(new BigDecimal("25000"), result.getServiceFee());
    }
    
    @Test
    @DisplayName("Should throw GhnApiException when fee calculation fails")
    void calculateFee_ApiFailure_ThrowsException() {
        // Given
        CalculateFeeRequest request = CalculateFeeRequest.builder()
            .toDistrictId(1442)
            .toWardCode("1A0001")
            .weight(1000)
            .build();
        
        GhnApiResponse<CalculateFeeResponse> response = new GhnApiResponse<>();
        response.setCode(400);
        response.setMessage("Invalid request");
        response.setData(null);
        
        ResponseEntity<GhnApiResponse<CalculateFeeResponse>> responseEntity = 
            new ResponseEntity<>(response, HttpStatus.OK);
        
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);
        
        // When & Then
        assertThrows(GhnApiClient.GhnApiException.class, () -> {
            ghnApiClient.calculateFee(request);
        });
    }
    
    @Test
    @DisplayName("Should throw IllegalArgumentException when request is null")
    void calculateFee_NullRequest_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            ghnApiClient.calculateFee(null);
        });
    }
}

