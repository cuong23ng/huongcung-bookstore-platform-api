package com.huongcung.webstore.checkout.service.impl;

import com.huongcung.webstore.checkout.dto.CalculateFeeResponseDTO;
import com.huongcung.webstore.checkout.external.ghn.GhnApiClient;
import com.huongcung.webstore.checkout.external.ghn.dto.*;
import com.huongcung.webstore.checkout.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnDeliveryServiceImpl implements DeliveryService {

    private final GhnApiClient ghnApiClient;

    @Override
    public List<GhnProvinceDTO> getProvinces() {
        return ghnApiClient.getProvinces();
    }

    @Override
    public List<GhnDistrictDTO> getDistricts(Integer provinceId) {
        return ghnApiClient.getDistricts(provinceId);
    }

    @Override
    public List<GhnWardDTO> getWards(Integer districtId) {
        return ghnApiClient.getWards(districtId);
    }

    @Override
    public List<GhnServiceDTO> getServices() {
        return ghnApiClient.getServices();
    }

    @Override
    public CalculateFeeResponseDTO calculateEstimatedDeliveryFee(String serviceTypeId, String toDistrictId, String toWardCode, Integer weight) {
        CalculateFeeRequest ghnRequest = CalculateFeeRequest.builder()
                .serviceTypeId(2)
                .serviceId(53321) // Standard service
                .toDistrictId(Integer.parseInt(toDistrictId))
                .toWardCode(toWardCode)
                .weight(weight)
                .build();

        CalculateFeeResponse response = ghnApiClient.calculateFee(ghnRequest);

        CalculateFeeResponseDTO dto = CalculateFeeResponseDTO.builder()
                .total(response.getTotal())
                .serviceFee(response.getServiceFee())
                .expectedDeliveryTime("2-3 days") // GHN API doesn't always return this
                .build();

        return dto;
    }
}
