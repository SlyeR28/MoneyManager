package org.moneymanagement.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.moneymanagement.Entity.Income;
import org.moneymanagement.Payload.Request.IncomeRequest;
import org.moneymanagement.Payload.Response.IncomeResponse;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "profileId", target = "profile.id")
    Income requestToEntity(IncomeRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "profile.id", target = "profileId")
    @Mapping(source = "profile.fullName", target = "profileName")
    IncomeResponse entityToResponse(Income income);
}
