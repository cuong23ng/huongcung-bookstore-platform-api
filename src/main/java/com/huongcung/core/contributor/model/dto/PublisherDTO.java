package com.huongcung.core.contributor.model.dto;

import com.huongcung.core.common.model.dto.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PublisherDTO extends BaseDTO {
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
}
