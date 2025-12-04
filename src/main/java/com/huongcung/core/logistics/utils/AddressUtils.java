package com.huongcung.core.logistics.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huongcung.core.logistics.model.dto.AddressDTO;
import com.huongcung.core.logistics.model.dto.DistrictDTO;
import com.huongcung.core.logistics.model.dto.ProvinceDTO;
import com.huongcung.core.logistics.model.dto.WardDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AddressUtils {

    public static AddressDTO parseAddressJson(String addressJson) {
        ObjectMapper mapper = new ObjectMapper();
        AddressDTO dto = new AddressDTO();

        try {
            JsonNode root = mapper.readTree(addressJson);

            if (root.has("fullName")) {
                dto.setName(root.get("fullName").asText());
            }

            if (root.has("phone")) {
                dto.setPhone(root.get("phone").asText());
            }

            if (root.has("address")) {
                dto.setAddress(root.get("address").asText());
            }

            if (root.has("provinceId") && !root.get("provinceId").isNull()) {
                ProvinceDTO p = new ProvinceDTO();
                p.setProvinceId(root.get("provinceId").asText());
                p.setProvinceName(root.has("provinceName") ? root.get("provinceName").asText() : null);
                dto.setProvince(p);
            }

            if (root.has("districtId") && !root.get("districtId").isNull()) {
                DistrictDTO d = new DistrictDTO();
                d.setDistrictId(root.get("districtId").asText());
                d.setDistrictName(root.has("districtName") ? root.get("districtName").asText() : null);
                dto.setDistrict(d);
            }

            if (root.has("wardCode") && !root.get("wardCode").isNull()) {
                WardDTO w = new WardDTO();
                w.setWardCode(root.get("wardCode").asText());
                w.setWardName(root.has("wardName") ? root.get("wardName").asText() : null);
                dto.setWard(w);
            }

        } catch (IOException e) {
            log.info("Lỗi khi parse JSON: {}", e.getMessage());
            return null;
        }

        return dto;
    }
}
