package com.huongcung.webstore.checkout.external.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.webstore.checkout.external.ghn.config.GhnApiConfig;
import com.huongcung.webstore.checkout.external.ghn.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnApiClient {
    
    private final GhnApiConfig ghnApiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Get all provinces from GHN API
     */
    public List<GhnProvinceDTO> getProvinces() {
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/province";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            log.debug("Calling GHN API: GET {}", url);
            ResponseEntity<GhnApiResponse<List<GhnProvinceDTO>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new org.springframework.core.ParameterizedTypeReference<GhnApiResponse<List<GhnProvinceDTO>>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GhnProvinceDTO>> apiResponse = response.getBody();
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
     * Get districts for a province
     */
    public List<GhnDistrictDTO> getDistricts(Integer provinceId) {
        if (provinceId == null) {
            throw new IllegalArgumentException("Province ID cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/district";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(Collections.singletonMap("province_id", provinceId), headers);
            
            log.debug("Calling GHN API: POST {} with province_id={}", url, provinceId);
            ResponseEntity<GhnApiResponse<List<GhnDistrictDTO>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<GhnApiResponse<List<GhnDistrictDTO>>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GhnDistrictDTO>> apiResponse = response.getBody();
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
     * Get wards for a district
     */
    public List<GhnWardDTO> getWards(Integer districtId) {
        if (districtId == null) {
            throw new IllegalArgumentException("District ID cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/master-data/ward";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(Collections.singletonMap("district_id", districtId), headers);
            
            log.debug("Calling GHN API: POST {} with district_id={}", url, districtId);
            ResponseEntity<GhnApiResponse<List<GhnWardDTO>>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<GhnApiResponse<List<GhnWardDTO>>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GhnApiResponse<List<GhnWardDTO>> apiResponse = response.getBody();
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
     * Calculate delivery fee
     */
    public CalculateFeeResponse calculateFee(CalculateFeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Calculate fee request cannot be null");
        }
        
        try {
            String url = ghnApiConfig.getBaseUrl() + "/shiip/public-api/v2/shipping-order/fee";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<CalculateFeeRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("Calling GHN API: POST {} with request: {}", url, request);
            ResponseEntity<GhnApiResponse<CalculateFeeResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new org.springframework.core.ParameterizedTypeReference<GhnApiResponse<CalculateFeeResponse>>() {}
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
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnApiConfig.getApiToken());
        if (ghnApiConfig.getShopId() != null) {
            headers.set("ShopId", String.valueOf(ghnApiConfig.getShopId()));
        }
        return headers;
    }
    
    public static class GhnApiException extends RuntimeException {
        public GhnApiException(String message) {
            super(message);
        }
        
        public GhnApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

