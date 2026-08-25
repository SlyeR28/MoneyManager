package org.moneymanagement.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.moneymanagement.entity.Income;
import org.moneymanagement.payload.request.IncomeRequest;
import org.moneymanagement.payload.response.IncomeResponse;

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
