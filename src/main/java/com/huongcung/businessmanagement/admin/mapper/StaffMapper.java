package com.huongcung.businessmanagement.admin.mapper;

import com.huongcung.businessmanagement.admin.model.StaffCreateRequest;
import com.huongcung.businessmanagement.admin.model.StaffDTO;
import com.huongcung.businessmanagement.admin.model.StaffUpdateRequest;
import com.huongcung.core.common.mapper.CommonMapper;
import com.huongcung.core.user.model.entity.StaffEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = { CommonMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StaffMapper {
    
    /**
     * Maps StaffEntity to StaffDTO
     * Excludes passwordHash as per AC7
     */
    StaffDTO toDTO(StaffEntity entity);
    
    /**
     * Maps StaffCreateRequest to StaffEntity
     * Note: Password encoding is handled in service layer, not in mapper
     * This method should not be used directly - password needs to be encoded first
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) // Password encoding handled in service
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "isActive", ignore = true) // Set to true by default in service
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hireDate", ignore = true) // Optional field
    StaffEntity toEntity(StaffCreateRequest request);
    
    /**
     * Updates StaffEntity with values from StaffUpdateRequest (partial update)
     * Only non-null fields from request will be applied to entity
     * Fields that shouldn't be updated are ignored
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "email", ignore = true) // Email cannot be updated
    @Mapping(target = "passwordHash", ignore = true) // Password update handled separately if needed
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    void updateEntityFromRequest(StaffUpdateRequest request, @MappingTarget StaffEntity entity);
}

