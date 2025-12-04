package com.huongcung.core.logistics.external.ghn.service;

import com.huongcung.core.logistics.external.ghn.dto.*;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;

import java.util.List;

public interface GhnService {
    List<GhnProvinceDTO> getProvinces();
    List<GhnDistrictDTO> getDistricts(Integer provinceId);
    List<GhnWardDTO> getWards(Integer districtId);
    List<GhnServiceDTO> getServices(Integer fromDistrictId, Integer toDistrictId);

    GhnCalculateFeeDTO calculateEstimatedDeliveryFee(Integer serviceTypeId,
                                                     Integer fromDistrictId, String fromWardCode,
                                                     Integer toDistrictId, String toWardCode,
                                                     Integer weight, Integer length, Integer width, Integer height);

    GhnCalculateExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(Integer fromDistrictId, Integer toDistrictId,
                                                                      String toWardCode, Integer serviceId);

    String createShippingOrder(ConsignmentEntity consignment);
}
