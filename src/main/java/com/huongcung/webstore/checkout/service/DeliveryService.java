package com.huongcung.webstore.checkout.service;

import com.huongcung.webstore.checkout.dto.CalculateFeeRequestDTO;
import com.huongcung.webstore.checkout.dto.CalculateFeeResponseDTO;
import com.huongcung.webstore.checkout.external.ghn.dto.*;

import java.util.List;

public interface DeliveryService {
    List<GhnProvinceDTO> getProvinces();
    List<GhnDistrictDTO> getDistricts(Integer provinceId);
    List<GhnWardDTO> getWards(Integer districtId);
    List<GhnServiceDTO> getServices();
    CalculateFeeResponseDTO calculateEstimatedDeliveryFee(String serviceTypeId, String toDistrictId, String toWardCode, Integer weight);
}
