package org.moneymanagement.Mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Payload.Request.ProfileRequest;
import org.moneymanagement.Payload.Response.ProfileResponse;


@Mapper(componentModel = "spring")
public interface ProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "activationCode", ignore = true)
    ProfileEntity toEntity(ProfileRequest request);


    ProfileResponse toResponse(ProfileEntity entity);

}
