package com.huongcung.core.user.model.domain;

import com.huongcung.core.common.enumeration.City;
import com.huongcung.core.user.enumeration.StaffType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class Staff extends User {
    private StaffType staffType;
    private City assignedCity;
    private LocalDate hireDate;
}
