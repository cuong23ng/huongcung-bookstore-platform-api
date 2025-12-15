package com.huongcung.core.common.utils;

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

    /**
     * Chuyển đổi address từ JSON thành string có format: [address], [ward], [district], [province]
     * Ví dụ: "85 Trần Thái Tông, phường Bạch Đằng, Quận 1, TP.HCM"
     * 
     * @param addressJson JSON string chứa thông tin địa chỉ
     * @return String địa chỉ đã được format, hoặc null nếu parse lỗi
     */
    public static String formatAddressString(String addressJson) {
        AddressDTO dto = parseAddressJson(addressJson);
        if (dto == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        // Thêm địa chỉ (số nhà, tên đường)
        if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
            sb.append(dto.getAddress().trim());
        }

        // Thêm phường/xã
        if (dto.getWard() != null && dto.getWard().getWardName() != null && !dto.getWard().getWardName().trim().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            String wardName = dto.getWard().getWardName().trim();
            String lowerWardName = wardName.toLowerCase();
            // Thêm prefix "phường" hoặc "xã" nếu chưa có
            if (!lowerWardName.startsWith("phường") && !lowerWardName.startsWith("xã") && !lowerWardName.startsWith("thị trấn")) {
                sb.append("Phường ");
            }
            sb.append(wardName);
        }

        // Thêm quận/huyện
        if (dto.getDistrict() != null && dto.getDistrict().getDistrictName() != null && !dto.getDistrict().getDistrictName().trim().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            String districtName = dto.getDistrict().getDistrictName().trim();
            String lowerDistrictName = districtName.toLowerCase();
            // Thêm prefix "Quận" hoặc "Huyện" nếu chưa có
            if (!lowerDistrictName.startsWith("quận")
                    && !lowerDistrictName.startsWith("huyện")
                    && !lowerDistrictName.startsWith("thành phố")
                    && !lowerDistrictName.startsWith("thị xã")) {
                sb.append("Quận ");
            }
            sb.append(districtName);
        }

        // Thêm thành phố/tỉnh
        if (dto.getProvince() != null && dto.getProvince().getProvinceName() != null && !dto.getProvince().getProvinceName().trim().isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(dto.getProvince().getProvinceName().trim());
        }

        return !sb.isEmpty() ? sb.toString() : null;
    }
}
