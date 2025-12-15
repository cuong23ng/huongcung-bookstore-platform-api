package com.huongcung.core.logistics.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressDTO {
    private String serviceTypeId;
    private String name;
    private String phone;
    private String address;
    private ProvinceDTO province;
    private DistrictDTO district;
    private WardDTO ward;
    private String postalCode;
}
