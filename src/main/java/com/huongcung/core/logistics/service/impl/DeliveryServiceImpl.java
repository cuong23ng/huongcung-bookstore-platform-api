package com.huongcung.core.logistics.service.impl;

import com.huongcung.core.logistics.external.ghn.dto.*;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.logistics.model.dto.*;
import com.huongcung.core.logistics.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final GhnService ghnService;

    @Override
    public List<ProvinceDTO> getProvinces() {
        List<GhnProvinceDTO> ghnProvinces = ghnService.getProvinces();
        return ghnProvinces.parallelStream()
                .map(ghnProvince -> ProvinceDTO.builder()
                        .provinceId(String.valueOf(ghnProvince.getProvinceId()))
                        .provinceName(ghnProvince.getProvinceName())
                        .build())
                .toList();
    }

    @Override
    public List<DistrictDTO> getDistricts(String provinceId) {
        List<GhnDistrictDTO> ghnDistricts = ghnService.getDistricts(Integer.valueOf(provinceId));
        return ghnDistricts.parallelStream()
                .map(ghnDistrict -> DistrictDTO.builder()
                        .districtId(String.valueOf(ghnDistrict.getDistrictId()))
                        .districtName(ghnDistrict.getDistrictName())
                        .build())
                .toList();
    }

    @Override
    public List<WardDTO> getWards(String wardId) {
        List<GhnWardDTO> ghnWards = ghnService.getWards(Integer.valueOf(wardId));
        return ghnWards.parallelStream()
                .map(ghnWard -> WardDTO.builder()
                        .wardCode(ghnWard.getWardCode())
                        .wardName(ghnWard.getWardName())
                        .build())
                .toList();
    }

    @Override
    public List<ServiceDTO> getServices(String toDistrictId) {
        Integer fromDistrictNum = null;
        Integer toDistrictIdNum = toDistrictId != null ? Integer.valueOf(toDistrictId) : null;
        List<GhnServiceDTO> ghnServices = ghnService.getServices(
                fromDistrictNum,
                toDistrictIdNum);
        return ghnServices.parallelStream()
                .map(ghnService -> ServiceDTO.builder()
                        .serviceId(String.valueOf(ghnService.getServiceId()))
                        .serviceTypeId(String.valueOf(ghnService.getServiceTypeId()))
                        .shortName(ghnService.getShortName())
                        .build())
                .toList();
    }

    @Override
    public CalculateFeeDTO calculateEstimatedDeliveryFee(String serviceTypeId, String toDistrictId, String toWardCode, Integer weight) {
        //TODO: Add from address
        Integer serviceTypeIdNum = Integer.valueOf(serviceTypeId);
        Integer toDistrictIdNum = Integer.valueOf(toDistrictId);
        GhnCalculateFeeDTO ghnCalculateFee = ghnService.calculateEstimatedDeliveryFee(serviceTypeIdNum, null, null,
                toDistrictIdNum, toWardCode, weight, 0, 0, 0);

        return CalculateFeeDTO.builder()
                .total(ghnCalculateFee.getTotal())
                .serviceFee(ghnCalculateFee.getServiceFee())
                .build();
    }
}
