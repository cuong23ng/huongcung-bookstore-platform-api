package com.huongcung.core.logistics.service;

import com.huongcung.core.logistics.model.dto.*;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import com.huongcung.storefront.checkout.dto.CheckoutItemDTO;
import com.huongcung.storefront.checkout.dto.ShippingAddressDTO;

import java.util.List;

public interface DeliveryService {
    List<ProvinceDTO> getProvinces();
    List<DistrictDTO> getDistricts(String provinceId);
    List<WardDTO> getWards(String districtId);
    List<ServiceDTO> getServices(String toDistrictId);

    CalculateFeeDTO calculateEstimatedDeliveryFee(String serviceTypeId,
                                                  String fromDistrictId, String fromWardCode,
                                                  String toDistrictId, String toWardCode,
                                                  Integer weight, Integer length, Integer width, Integer height);

    CalculateFeeDTO calculateEstimatedDeliveryFee(List<OrderEntryEntity> entries,
                                                  AddressDTO warehouseAddress,
                                                  AddressDTO customerAddress);

    ExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(List<OrderEntryEntity> entries,
                                                          AddressDTO warehouseAddress,
                                                          AddressDTO customerAddress);

    ShippingOrderDTO createShippingOrder(ConsignmentEntity consignment);
}
