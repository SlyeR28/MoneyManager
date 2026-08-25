package org.moneymanagement.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.payload.request.ProfileRequest;
import org.moneymanagement.payload.response.ProfileResponse;


@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "activationToken", ignore = true)
    ProfileEntity toEntity(ProfileRequest request);


    ProfileResponse toResponse(ProfileEntity entity);

}
