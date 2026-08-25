package org.moneymanagement.service;

import org.moneymanagement.payload.request.CategoryRequest;
import org.moneymanagement.payload.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory( Long categoryId ,CategoryRequest categoryRequest);
    void deleteCategory(Long id);
    CategoryResponse findById(Long id);
    List<CategoryResponse> getCategoiesForCurrentUser();
    List<CategoryResponse> getCategoiesByTypeForCurrentUser(String type);

}
