package com.huongcung.core.logistics.external.ghn.service.impl;

import com.huongcung.core.logistics.external.ghn.client.GhnApiClient;
import com.huongcung.core.logistics.external.ghn.dto.*;
import com.huongcung.core.logistics.external.ghn.dto.request.*;
import com.huongcung.core.logistics.external.ghn.dto.response.*;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.common.utils.AddressUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GhnServiceImpl implements GhnService {

    private final GhnApiClient ghnApiClient;

    @Override
    public List<GhnProvinceDTO> getProvinces() {
        List<GetProvinceResponse> provinceResponses = ghnApiClient.getProvinces();
        return provinceResponses.parallelStream()
                .map(r -> GhnProvinceDTO.builder()
                        .provinceName(r.getProvinceName())
                        .provinceId(r.getProvinceId())
                        .build())
                .toList();
    }

    @Override
    public List<GhnDistrictDTO> getDistricts(Integer provinceId) {
        List<GetDistrictResponse> districtResponses = ghnApiClient.getDistricts(provinceId);
        return districtResponses.parallelStream()
                .map(d -> GhnDistrictDTO.builder()
                        .districtName(d.getDistrictName())
                        .districtId(d.getDistrictId())
                        .build())
                .toList();
    }

    @Override
    public List<GhnWardDTO> getWards(Integer districtId) {
        List<GetWardResponse> wardResponses = ghnApiClient.getWards(districtId);
        return wardResponses.parallelStream()
                .map(w -> GhnWardDTO.builder()
                        .wardCode(w.getWardCode())
                        .wardName(w.getWardName())
                        .build())
                .toList();
    }

    @Override
    public List<GhnServiceDTO> getServices(Integer fromDistrictId, Integer toDistrictId) {
        GetServiceRequest request = GetServiceRequest.builder()
                .fromDistrict(fromDistrictId)
                .toDistrict(toDistrictId)
                .build();
        List<GetServiceResponse> serviceResponses = ghnApiClient.getServices(request);
        return serviceResponses.parallelStream()
                .map(s -> GhnServiceDTO.builder()
                        .serviceTypeId(s.getServiceTypeId())
                        .serviceId(s.getServiceId())
                        .shortName(s.getShortName())
                        .build())
                .toList();
    }

    @Override
    public GhnCalculateFeeDTO calculateEstimatedDeliveryFee(Integer serviceTypeId,
                                                            Integer fromDistrictId, String fromWardCode,
                                                            Integer toDistrictId, String toWardCode,
                                                            Integer weight, Integer length, Integer width, Integer height) {
        CalculateFeeRequest ghnRequest = CalculateFeeRequest.builder()
                .serviceTypeId(serviceTypeId) //TODO: Set based on service customer chosen
                .fromDistrictId(fromDistrictId)
                .fromWardCode(fromWardCode)
                .toDistrictId(toDistrictId)
                .toWardCode(toWardCode)
                .weight(weight)
                .length(length)
                .width(width)
                .height(height)
                .build();

        CalculateFeeResponse response = ghnApiClient.calculateFee(ghnRequest);

        return GhnCalculateFeeDTO.builder()
                .total(response.getTotal())
                .serviceFee(response.getServiceFee())
                .build();
    }

    @Override
    public GhnCalculateExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(Integer serviceTypeId,
                                                                             Integer fromDistrictId, String fromWardCode,
                                                                             Integer toDistrictId, String toWardCode,
                                                                             Integer weight) {

        CalculateExpectedDeliveryTimeRequest request = CalculateExpectedDeliveryTimeRequest.builder()
                .fromDistrictId(fromDistrictId)
                .fromWardCode(fromWardCode)
                .toDistrictId(toDistrictId)
                .toWardCode(toWardCode)
                .serviceTypeId(serviceTypeId)
                .weight(weight)
                .build();

        CalculateExpectedDeliveryTimeResponse response = ghnApiClient.calculateExpectedDeliveryTime(request);

        return GhnCalculateExpectedDeliveryTimeDTO.builder()
                .leadTime(response.getLeadTime())
                .orderDate(response.getOrderDate())
                .build();
    }

    @Override
    public CreateShippingOrderResponse createShippingOrder(Integer serviceTypeId, Integer paymentTypeId,
                                                    String fromName, String fromPhone, String fromProvinceName, String fromDistrictName, String fromWardName, String fromAddress,
                                                    String toName, String toPhone, Integer toDistrictId, String toWardCode, String toAddress,
                                                    Integer weight, Integer height, Integer length, Integer width,
                                                    Integer codAmount, String clientOrderCode, String requiredNote, List<ShippingItemRequest> items) {
        CreateShippingOrderRequest request = CreateShippingOrderRequest.builder()
                .fromName(fromName)
                .fromPhone(fromPhone)
                .fromProvinceName(fromProvinceName)
                .fromDistrictName(fromDistrictName)
                .fromWardName(fromWardName)
                .fromAddress(fromAddress)
                .toName(toName)
                .toPhone(toPhone)
                .toDistrictId(toDistrictId)
                .toWardCode(toWardCode)
                .toAddress(toAddress)
                .clientOrderCode(clientOrderCode)
                .codAmount(codAmount)
                .weight(weight)
                .height(height)
                .length(length)
                .width(width)
                .serviceTypeId(serviceTypeId)
                .paymentTypeId(paymentTypeId)
                .requiredNote(requiredNote)
                .items(items)
                .build();

        CreateShippingOrderResponse response = ghnApiClient.createShippingOrder(request);
        return response;
    }

    public ShippingItemRequest createShippingItem(String name, String code,
                                                  Integer quantity, Integer price,
                                                  Integer height, Integer weight, Integer length, Integer width) {
        return ShippingItemRequest.builder()
                .name(name)
                .code(code)
                .quantity(quantity)
                .price(price)
                .height(height)
                .weight(weight)
                .length(length)
                .width(width)
                .build();
    }

    public String getOrderStatus(String trackingNumber) {
        GetOrderStatusRequest request = GetOrderStatusRequest.builder()
                .orderCode(trackingNumber)
                .build();
        GetOrderStatusResponse response = ghnApiClient.getOrderStatus(request);
        return response.getStatus();
    }
}
