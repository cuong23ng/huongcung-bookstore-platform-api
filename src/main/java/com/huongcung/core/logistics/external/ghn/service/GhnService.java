package com.huongcung.core.logistics.external.ghn.service;

import com.huongcung.core.logistics.external.ghn.dto.*;
import com.huongcung.core.logistics.external.ghn.dto.request.ShippingItemRequest;
import com.huongcung.core.logistics.external.ghn.dto.response.CreateShippingOrderResponse;

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

    GhnCalculateExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(Integer serviceTypeId,
                                                                      Integer fromDistrictId, String fromWardCode,
                                                                      Integer toDistrictId, String toWardCode,
                                                                      Integer weight);

    CreateShippingOrderResponse createShippingOrder(Integer serviceTypeId, Integer paymentTypeId,
                                                    String fromName, String fromPhone, String fromProvinceName, String fromDistrictName, String fromWardName, String fromAddress,
                                                    String toName, String toPhone, Integer toDistrictId, String toWardCode, String toAddress,
                                                    Integer weight, Integer height, Integer length, Integer width,
                                                    Integer codAmount, String clientOrderCode, String requiredNote, List<ShippingItemRequest> items);

    ShippingItemRequest createShippingItem(String name, String code,
                                           Integer quantity, Integer price,
                                           Integer height, Integer weight, Integer length, Integer width);

    String getOrderStatus(String trackingNumber);
}
