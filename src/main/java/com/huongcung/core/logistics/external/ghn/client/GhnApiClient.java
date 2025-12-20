package com.huongcung.core.logistics.external.ghn.client;

import com.huongcung.core.logistics.external.ghn.dto.request.CalculateExpectedDeliveryTimeRequest;
import com.huongcung.core.logistics.external.ghn.dto.request.CalculateFeeRequest;
import com.huongcung.core.logistics.external.ghn.dto.request.CreateShippingOrderRequest;
import com.huongcung.core.logistics.external.ghn.dto.request.GetOrderStatusRequest;
import com.huongcung.core.logistics.external.ghn.dto.request.GetServiceRequest;
import com.huongcung.core.logistics.external.ghn.dto.response.*;
import com.huongcung.core.logistics.external.ghn.exception.GhnApiException;
import com.huongcung.core.logistics.external.ghn.configuration.GhnApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnApiClient {
    
    private final GhnApiConfig ghnApiConfig;
    private final RestTemplate restTemplate;

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=77">API Get Service</a>
     */
    public List<GetServiceResponse> getServices(GetServiceRequest request) {
        // API lỗi nên mock data
        GetServiceResponse service1 = GetServiceResponse.builder()
                .serviceTypeId(2)
                .serviceId(53320)
                .shortName("Tiêu chuẩn")
                .build();
        GetServiceResponse service2 = GetServiceResponse.builder()
                .serviceTypeId(1)
                .serviceId(53319)
                .shortName("Nhanh")
                .build();
        GetServiceResponse service3 = GetServiceResponse.builder()
                .serviceTypeId(3)
                .serviceId(53321)
                .shortName("Tiết kiệm")
                .build();
        return List.of(service1, service2, service3);
    }
    
    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=60">API Get Province</a>
     */
    public List<GetProvinceResponse> getProvinces() {
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/province";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            log.debug("Calling GHN API: GET {}", url);
            ResponseEntity<GhnApiResponse<List<GetProvinceResponse>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GetProvinceResponse>> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API provinces response: {} provinces", apiResponse.getData().size());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}", 
                        apiResponse.getCode(), apiResponse.getMessage());
                    return Collections.emptyList();
                }
            }
            
            log.warn("GHN API provinces call failed with status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (RestClientException e) {
            log.error("Error calling GHN API for provinces: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to fetch provinces from GHN API", e);
        }
    }
    
    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=78">API Get District</a>
     */
    public List<GetDistrictResponse> getDistricts(Integer provinceId) {
        if (provinceId == null) {
            throw new IllegalArgumentException("Province ID cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/district";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(Collections.singletonMap("province_id", provinceId), headers);
            
            log.debug("Calling GHN API: POST {} with province_id={}", url, provinceId);
            ResponseEntity<GhnApiResponse<List<GetDistrictResponse>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GetDistrictResponse>> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API districts response: {} districts", apiResponse.getData().size());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}", 
                        apiResponse.getCode(), apiResponse.getMessage());
                    return Collections.emptyList();
                }
            }
            
            log.warn("GHN API districts call failed with status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (RestClientException e) {
            log.error("Error calling GHN API for districts: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to fetch districts from GHN API", e);
        }
    }

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=61">API Get Ward</a>
     */
    public List<GetWardResponse> getWards(Integer districtId) {
        if (districtId == null) {
            throw new IllegalArgumentException("District ID cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/ward";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(Collections.singletonMap("district_id", districtId), headers);
            
            log.debug("Calling GHN API: POST {} with district_id={}", url, districtId);
            ResponseEntity<GhnApiResponse<List<GetWardResponse>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GetWardResponse>> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API wards response: {} wards", apiResponse.getData().size());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}", 
                        apiResponse.getCode(), apiResponse.getMessage());
                    return Collections.emptyList();
                }
            }
            
            log.warn("GHN API wards call failed with status: {}", response.getStatusCode());
            return Collections.emptyList();
            
        } catch (RestClientException e) {
            log.error("Error calling GHN API for wards: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to fetch wards from GHN API", e);
        }
    }

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=76">API Calculate Fee</a>
     */
    public CalculateFeeResponse calculateFee(CalculateFeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Calculate fee request cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/v2/shipping-order/fee";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<CalculateFeeRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling GHN API: POST {} with request: {}", url, request);
            ResponseEntity<GhnApiResponse<CalculateFeeResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<CalculateFeeResponse> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API fee calculation response: total={}", apiResponse.getData().getTotal());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}", 
                        apiResponse.getCode(), apiResponse.getMessage());
                    throw new GhnApiException("GHN API fee calculation failed: " + apiResponse.getMessage());
                }
            }
            
            log.warn("GHN API fee calculation call failed with status: {}", response.getStatusCode());
            throw new GhnApiException("GHN API fee calculation failed with status: " + response.getStatusCode());
            
        } catch (RestClientException e) {
            log.error("Error calling GHN API for fee calculation: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to calculate fee from GHN API", e);
        }
    }

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=52">API Calculate the expected delivery time</a>
     */
    public CalculateExpectedDeliveryTimeResponse calculateExpectedDeliveryTime(CalculateExpectedDeliveryTimeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CalculateExpectedDeliveryTimeRequest cannot be null");
        }

        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/v2/shipping-order/leadtime";

            HttpHeaders headers = createHeaders();
            HttpEntity<CalculateExpectedDeliveryTimeRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling GHN API: POST {} with request: {}", url, request);
            ResponseEntity<GhnApiResponse<CalculateExpectedDeliveryTimeResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<CalculateExpectedDeliveryTimeResponse> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API Calculate the expected delivery time response: leadtime={}",
                            apiResponse.getData().getLeadTime());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}",
                            apiResponse.getCode(), apiResponse.getMessage());
                    throw new GhnApiException("GHN API Calculate the expected delivery time failed: " + apiResponse.getMessage());
                }
            }

            log.warn("GHN API Calculate the expected delivery time failed with status: {}", response.getStatusCode());
            throw new GhnApiException("GHN API Calculate the expected delivery time failed with status: " + response.getStatusCode());

        } catch (RestClientException e) {
            log.error("Error calling GHN API for calculating the expected delivery time: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to calculate the expected delivery time from GHN API", e);
        }
    }

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=123">API Create Shipping Order</a>
     */
    public CreateShippingOrderResponse createShippingOrder(CreateShippingOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateShippingOrderRequest cannot be null");
        }

        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/v2/shipping-order/create";

            HttpHeaders headers = createHeaders();
            HttpEntity<CreateShippingOrderRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling GHN API: POST {} with request: {}", url, request);
            ResponseEntity<GhnApiResponse<CreateShippingOrderResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<CreateShippingOrderResponse> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API create shipping order response: order code={}",
                            apiResponse.getData().getOrderCode());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}",
                            apiResponse.getCode(), apiResponse.getMessage());
                    throw new GhnApiException("GHN API create shipping order failed: " + apiResponse.getMessage());
                }
            }

            log.warn("GHN API create shipping order failed with status: {}", response.getStatusCode());
            throw new GhnApiException("GHN API create shipping order failed with status: " + response.getStatusCode());

        } catch (RestClientException e) {
            log.info("Error calling GHN API for creating shipping order: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to create shipping order from GHN API", e);
        }
    }

    /**
     * <a href="https://api.ghn.vn/home/docs/detail?id=66">API Order Info</a>
     */
    public GetOrderStatusResponse getOrderStatus(GetOrderStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("GetOrderStatusRequest cannot be null");
        }

        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/v2/shipping-order/detail";

            HttpHeaders headers = createHeaders();
            HttpEntity<GetOrderStatusRequest> entity = new HttpEntity<>(request, headers);

            log.info("Calling GHN API: POST {} with request: {}", url, request);
            ResponseEntity<GhnApiResponse<GetOrderStatusResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<GetOrderStatusResponse> apiResponse = response.getBody();
                if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                    log.debug("GHN API get order status response: order_code={}, status={}",
                            apiResponse.getData().getOrderCode(), apiResponse.getData().getStatus());
                    return apiResponse.getData();
                } else {
                    log.warn("GHN API returned non-success code: {}, message: {}",
                            apiResponse.getCode(), apiResponse.getMessage());
                    throw new GhnApiException("GHN API get order status failed: " + apiResponse.getMessage());
                }
            }

            log.warn("GHN API get order status failed with status: {}", response.getStatusCode());
            throw new GhnApiException("GHN API get order status failed with status: " + response.getStatusCode());

        } catch (RestClientException e) {
            log.error("Error calling GHN API for getting order status: {}", e.getMessage(), e);
            throw new GhnApiException("Failed to get order status from GHN API", e);
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiConfig.getApiToken());
        if (ghnApiConfig.getShopId() != null) {
            headers.set("ShopId", String.valueOf(ghnApiConfig.getShopId()));
        }
        return headers;
    }
}

