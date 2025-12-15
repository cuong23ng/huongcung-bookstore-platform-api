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
    public GhnCalculateExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(Integer fromDistrictId, Integer toDistrictId,
                                                                             String toWardCode, Integer serviceId) {

        CalculateExpectedDeliveryTimeRequest request = CalculateExpectedDeliveryTimeRequest.builder()
                .fromDistrictId(fromDistrictId)
                .toDistrictId(toDistrictId)
                .toWardCode(toWardCode)
                .serviceId(serviceId)
                .build();

        CalculateExpectedDeliveryTimeResponse response = ghnApiClient.calculateExpectedDeliveryTime(request);

        return GhnCalculateExpectedDeliveryTimeDTO.builder()
                .leadTime(response.getLeadTime())
                .orderDate(response.getOrderDate())
                .build();
    }

    @Override
    public String createShippingOrder(ConsignmentEntity consignment) {
        AddressDTO warehouseAddressDTO = AddressUtils.parseAddressJson(consignment.getOriginWarehouse().getAddress());
        AddressDTO customerAddressDTO = AddressUtils.parseAddressJson(consignment.getShippingAddress());
        CreateShippingOrderRequest request = CreateShippingOrderRequest.builder()
                .fromName(warehouseAddressDTO.getName())
                .fromPhone(warehouseAddressDTO.getPhone())
                .fromProvinceName(warehouseAddressDTO.getProvince().getProvinceName())
                .fromDistrictName(warehouseAddressDTO.getDistrict().getDistrictName())
                .fromWardName(warehouseAddressDTO.getWard().getWardName())
                .fromAddress(warehouseAddressDTO.getAddress())
                .toName(customerAddressDTO.getName())
                .toPhone(customerAddressDTO.getPhone())
                .toDistrictId(Integer.valueOf(customerAddressDTO.getDistrict().getDistrictId()))
                .toWardCode(customerAddressDTO.getWard().getWardCode())
                .toAddress(customerAddressDTO.getAddress())
                .clientOrderCode(consignment.getCode())
                .codAmount(consignment.getCodAmount().toBigInteger().intValue())
                .weight(2)
                .height(2)
                .length(10)
                .width(2)
                .serviceTypeId(2)
                .paymentTypeId(2)
                .requiredNote("CHOXEMHANGKHONGTHU")
                .build();

        List<ShippingItemRequest> items = consignment.getEntries().stream()
                .map(e -> ShippingItemRequest.builder()
                        .name(e.getOrderEntry().getBook().getTitle())
                        .code(e.getOrderEntry().getBook().getCode())
                        .quantity(e.getQuantity())
                        .price(e.getQuantity() * e.getOrderEntry().getUnitPrice().intValue())
                        .height(e.getOrderEntry().getBook().getPhysicalBookInfo().getHeightCm())
                        .weight(e.getOrderEntry().getBook().getPhysicalBookInfo().getWeightGrams())
                        .length(e.getOrderEntry().getBook().getPhysicalBookInfo().getLengthCm())
                        .width(e.getOrderEntry().getBook().getPhysicalBookInfo().getWidthCm())
                        .build())
                .toList();

        request.setItems(items);
        CreateShippingOrderResponse response = ghnApiClient.createShippingOrder(request);
        return response.getOrderCode();
    }

    public String getOrderStatus(String trackingNumber) {
        GetOrderStatusRequest request = GetOrderStatusRequest.builder()
                .orderCode(trackingNumber)
                .build();
        GetOrderStatusResponse response = ghnApiClient.getOrderStatus(request);
        return response.getStatus();
    }
}
