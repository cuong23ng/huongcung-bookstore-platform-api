package com.huongcung.storefront.checkout.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.model.dto.DistrictDTO;
import com.huongcung.core.logistics.model.dto.ProvinceDTO;
import com.huongcung.core.logistics.model.dto.WardDTO;
import com.huongcung.storefront.checkout.dto.ShippingAddressDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@NoArgsConstructor
public class AddressUtils {
    public static ShippingAddressDTO parseShippingAddressJson(String addressJson) {
        ObjectMapper mapper = new ObjectMapper();
        ShippingAddressDTO dto = new ShippingAddressDTO();

        try {
            JsonNode root = mapper.readTree(addressJson);

            if (root.has("fullName")) {
                dto.setFullName(root.get("fullName").asText());
            }

            if (root.has("phone")) {
                dto.setPhone(root.get("phone").asText());
            }

            if (root.has("address")) {
                dto.setAddress(root.get("address").asText());
            }

            if (root.has("provinceId") && !root.get("provinceId").isNull()) {
                dto.setProvinceId(root.get("provinceId").asText());
                if (root.has("provinceName")) {
                    dto.setProvinceName(root.get("provinceName").asText());
                }
            }

            if (root.has("districtId") && !root.get("districtId").isNull()) {
                dto.setDistrictId(root.get("districtId").asText());
                if (root.has("districtName")) {
                    dto.setDistrictName(root.get("districtName").asText());
                }
            }

            if (root.has("wardCode") && !root.get("wardCode").isNull()) {
                dto.setWardCode(root.get("wardCode").asText());
                if (root.has("wardName")) {
                    dto.setWardName(root.get("wardName").asText());
                }
            }

            if (root.has("serviceTypeId")) {
                dto.setServiceTypeId(root.get("serviceTypeId").asText());
            }

            if (root.has("postalCode")) {
                dto.setPostalCode(root.get("postalCode").asText());
            }

        } catch (IOException e) {
            log.error("Lỗi khi parse JSON: {}", e.getMessage());
            return null;
        }

        return dto;
    }
    
    public static void populateAddress(ShippingAddressDTO source, AddressDTO target) {
        // Set basic address fields
        target.setAddress(source.getAddress());
        target.setName(source.getFullName());
        target.setPhone(source.getPhone());
        target.setPostalCode(source.getPostalCode());
        target.setServiceTypeId(source.getServiceTypeId());
        
        // Build and set WardDTO
        if (source.getWardCode() != null) {
            WardDTO ward = WardDTO.builder()
                    .wardCode(source.getWardCode())
                    .wardName(source.getWardName())
                    .build();
            target.setWard(ward);
        }
        
        // Build and set DistrictDTO
        if (source.getDistrictId() != null) {
            DistrictDTO district = DistrictDTO.builder()
                    .districtId(source.getDistrictId())
                    .districtName(source.getDistrictName())
                    .build();
            target.setDistrict(district);
        }
        
        // Build and set ProvinceDTO
        if (source.getProvinceId() != null) {
            ProvinceDTO province = ProvinceDTO.builder()
                    .provinceId(source.getProvinceId())
                    .provinceName(source.getProvinceName())
                    .build();
            target.setProvince(province);
        }
    }
}
