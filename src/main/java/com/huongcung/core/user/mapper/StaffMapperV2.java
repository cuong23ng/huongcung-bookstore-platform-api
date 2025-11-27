package com.huongcung.core.user.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.user.model.domain.Staff;
import com.huongcung.core.user.model.entity.StaffEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StaffMapperV2 extends DomainMapper<StaffEntity, Staff> {
}
