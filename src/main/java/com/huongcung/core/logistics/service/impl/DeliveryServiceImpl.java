package com.huongcung.core.logistics.service.impl;

import com.huongcung.core.catalog.model.entity.PhysicalBookEntity;
import com.huongcung.core.common.utils.AddressUtils;
import com.huongcung.core.logistics.external.ghn.dto.*;
import com.huongcung.core.logistics.external.ghn.dto.request.ShippingItemRequest;
import com.huongcung.core.logistics.external.ghn.dto.response.CreateShippingOrderResponse;
import com.huongcung.core.logistics.external.ghn.service.GhnService;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.model.dto.CalculateFeeDTO;
import com.huongcung.core.logistics.model.dto.DistrictDTO;
import com.huongcung.core.logistics.model.dto.ExpectedDeliveryTimeDTO;
import com.huongcung.core.logistics.model.dto.ProvinceDTO;
import com.huongcung.core.logistics.model.dto.ServiceDTO;
import com.huongcung.core.logistics.model.dto.ShippingOrderDTO;
import com.huongcung.core.logistics.model.dto.WardDTO;
import com.huongcung.core.logistics.model.entity.ConsignmentEntity;
import com.huongcung.core.logistics.model.entity.ConsignmentEntryEntity;
import com.huongcung.core.logistics.service.DeliveryService;
import com.huongcung.core.order.enumeration.PaymentMethod;
import com.huongcung.core.order.model.entity.OrderEntryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public CalculateFeeDTO calculateEstimatedDeliveryFee(String serviceTypeId,
                                                         String fromDistrictId, String fromWardCode,
                                                         String toDistrictId, String toWardCode,
                                                         Integer weight, Integer length, Integer width, Integer height) {
        Integer fromDistrictIdNum = 1687;
        Integer serviceTypeIdNum = Integer.valueOf(serviceTypeId);
        Integer toDistrictIdNum = Integer.valueOf(toDistrictId);
        GhnCalculateFeeDTO ghnCalculateFee = ghnService.calculateEstimatedDeliveryFee(serviceTypeIdNum, fromDistrictIdNum, fromWardCode,
                toDistrictIdNum, toWardCode, weight, length, width, height);

        return CalculateFeeDTO.builder()
                .total(ghnCalculateFee.getTotal())
                .serviceFee(ghnCalculateFee.getServiceFee())
                .build();
    }

    @Override
    public CalculateFeeDTO calculateEstimatedDeliveryFee(List<OrderEntryEntity> entries,
                                                         AddressDTO warehouseAddress,
                                                         AddressDTO customerAddress) {
        // Get warehouse district if available
        Integer fromDistrictId = Integer.valueOf(warehouseAddress.getDistrict().getDistrictId());
        String fromWardCode = warehouseAddress.getWard().getWardCode();

        Integer toDistrictId = Integer.valueOf(customerAddress.getDistrict().getDistrictId());
        String toWardCode = customerAddress.getWard().getWardCode();

        int totalWeight = entries.stream()
                .mapToInt(entry -> {
                    if (entry.getBook() != null && entry.getBook().getPhysicalBookInfo() != null) {
                        Integer weightGrams = entry.getBook().getPhysicalBookInfo().getWeightGrams();
                        if (weightGrams != null) {
                            return weightGrams * entry.getQuantity();
                        }
                    }
                    return 500 * entry.getQuantity(); // Default 500g per book
                })
                .sum();

        Integer serviceTypeId = customerAddress.getServiceTypeId() != null ?
                Integer.parseInt(customerAddress.getServiceTypeId()) : 2;

        GhnCalculateFeeDTO calculateFeeDTO = ghnService.calculateEstimatedDeliveryFee(
                serviceTypeId,
                fromDistrictId,
                fromWardCode,
                toDistrictId,
                toWardCode,
                totalWeight,
                10,
                2,
                2);

        return CalculateFeeDTO.builder()
                .total(calculateFeeDTO.getTotal())
                .serviceFee(calculateFeeDTO.getServiceFee())
                .build();
    }

    @Override
    public ExpectedDeliveryTimeDTO calculateExpectedDeliveryTime(List<OrderEntryEntity> entries,
                                                                 AddressDTO warehouseAddress,
                                                                 AddressDTO customerAddress) {

        // Get warehouse district if available
        Integer fromDistrictId = Integer.valueOf(warehouseAddress.getDistrict().getDistrictId());
        String fromWardCode = warehouseAddress.getWard().getWardCode();

        Integer toDistrictId = Integer.valueOf(customerAddress.getDistrict().getDistrictId());
        String toWardCode = customerAddress.getWard().getWardCode();

        int totalWeight = entries.stream()
                .mapToInt(entry -> {
                    if (entry.getBook() != null && entry.getBook().getPhysicalBookInfo() != null) {
                        Integer weightGrams = entry.getBook().getPhysicalBookInfo().getWeightGrams();
                        if (weightGrams != null) {
                            return weightGrams * entry.getQuantity();
                        }
                    }
                    return 500 * entry.getQuantity(); // Default 500g per book
                })
                .sum();

        Integer serviceTypeId = customerAddress.getServiceTypeId() != null ?
                Integer.parseInt(customerAddress.getServiceTypeId()) : 2;

        // Call GHN API to calculate expected delivery time based on warehouse and entries
        GhnCalculateExpectedDeliveryTimeDTO deliveryTimeDTO = ghnService.calculateExpectedDeliveryTime(
                serviceTypeId,
                fromDistrictId,
                fromWardCode,
                toDistrictId,
                toWardCode,
                totalWeight
        );

        return ExpectedDeliveryTimeDTO.builder()
                .leadTime(deliveryTimeDTO.getLeadTime())
                .orderDate(deliveryTimeDTO.getOrderDate()).build();
    }

    @Override
    public ShippingOrderDTO createShippingOrder(ConsignmentEntity consignment) {
        log.info("Creating shipping order for consignment {}", consignment.getCode());

        AddressDTO warehouseAddress = AddressUtils.parseAddressJson(consignment.getOriginWarehouse().getAddress());
        AddressDTO customerAddress = AddressUtils.parseAddressJson(consignment.getShippingAddress());

        if (warehouseAddress == null || customerAddress == null) {
            throw new IllegalStateException("Cannot parse addresses for consignment " + consignment.getCode());
        }

        int totalWeight = 0;
        int totalHeight = 0;
        int totalLength = 0;
        int totalWidth = 0;
        List<ShippingItemRequest> items = new ArrayList<>();

        //TODO: Consignment for ebook
        if (consignment.getEntries() != null && !consignment.getEntries().isEmpty()) {
            for (ConsignmentEntryEntity entry : consignment.getEntries()) {
                if (entry.getOrderEntry().getBook().getPhysicalBookInfo() == null) {
                    continue;
                }

                PhysicalBookEntity physicalBook = entry.getOrderEntry().getBook().getPhysicalBookInfo();

                int itemWeight = Optional.ofNullable(physicalBook.getWeightGrams()).orElse(500);
                int itemHeight = Optional.ofNullable(physicalBook.getHeightCm()).orElse(2);
                int itemLength = Optional.ofNullable(physicalBook.getLengthCm()).orElse(10);
                int itemWidth = Optional.ofNullable(physicalBook.getWidthCm()).orElse(2);

                int quantity = entry.getQuantity();
                totalWeight += itemWeight * quantity;
                totalHeight = Math.max(totalHeight, itemHeight);
                totalLength = Math.max(totalLength, itemLength);
                totalWidth = Math.max(totalWidth, itemWidth);

                ShippingItemRequest item =
                        ghnService.createShippingItem(
                                entry.getOrderEntry().getBook().getTitle(),
                                entry.getOrderEntry().getBook().getCode(),
                                quantity,
                                entry.getOrderEntry().getUnitPrice().multiply(BigDecimal.valueOf(quantity)).intValue(),
                                itemHeight,
                                itemWeight,
                                itemLength,
                                itemWidth
                        );
                items.add(item);
            }
        }

        totalWeight = Math.min(totalWeight, 1000);

        Integer serviceTypeId = customerAddress.getServiceTypeId() != null ?
                Integer.parseInt(customerAddress.getServiceTypeId()) : 2;
        Integer paymentTypeId = consignment.getOrder().getPaymentMethod() != PaymentMethod.COD ? 1 : 2;

        Integer codAmount = consignment.getCodAmount().toBigInteger().intValue();

        CreateShippingOrderResponse response =
                ghnService.createShippingOrder(
                        serviceTypeId,
                        paymentTypeId,
                        warehouseAddress.getName(),
                        warehouseAddress.getPhone(),
                        warehouseAddress.getProvince().getProvinceName(),
                        warehouseAddress.getDistrict().getDistrictName(),
                        warehouseAddress.getWard().getWardName(),
                        warehouseAddress.getAddress(),
                        customerAddress.getName(),
                        customerAddress.getPhone(),
                        Integer.valueOf(customerAddress.getDistrict().getDistrictId()),
                        customerAddress.getWard().getWardCode(),
                        customerAddress.getAddress(),
                        totalWeight,
                        totalHeight,
                        totalLength,
                        totalWidth,
                        codAmount,
                        consignment.getCode(),
                        "CHOXEMHANGKHONGTHU",
                        items
                );

        return ShippingOrderDTO.builder()
                .orderCode(response.getOrderCode())
                .expectedDeliveryTime(response.getExpectedDeliveryTime())
                .totalFee(response.getTotalFee())
                .build();
    }
}
