package com.huongcung.core.user.model.domain;

import com.huongcung.core.common.model.domain.BaseDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends BaseDomain {
    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
}
