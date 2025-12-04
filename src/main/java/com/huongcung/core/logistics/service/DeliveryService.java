package com.huongcung.core.logistics.service;

import com.huongcung.core.logistics.model.dto.*;

import java.util.List;

public interface DeliveryService {
    List<ProvinceDTO> getProvinces();
    List<DistrictDTO> getDistricts(String provinceId);
    List<WardDTO> getWards(String districtId);
    List<ServiceDTO> getServices(String toDistrictId);
    CalculateFeeDTO calculateEstimatedDeliveryFee(String serviceTypeId,
                                                  String toDistrictId, String toWardCode,
                                                  Integer weight);
}
