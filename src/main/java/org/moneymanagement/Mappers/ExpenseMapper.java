package org.moneymanagement.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.moneymanagement.Entity.Expense;
import org.moneymanagement.Payload.Request.ExpenseRequest;
import org.moneymanagement.Payload.Response.ExpenseResponse;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "profileId", target = "profile.id")
    Expense requestToEntity(ExpenseRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "profile.id", target = "profileId")
    @Mapping(source = "profile.fullName", target = "profileName")
    ExpenseResponse entityToResponse(Expense expense);
}
