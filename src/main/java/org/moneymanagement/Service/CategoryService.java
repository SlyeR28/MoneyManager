package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest) throws RuntimeException;
    CategoryResponse updateCategory( Long categoryId ,CategoryRequest categoryRequest);
    void deleteCategory(Long id);
    CategoryResponse findById(Long id);
    List<CategoryResponse> getCategoiesForCurrentUser();
    List<CategoryResponse> getCategoiesByTypeForCurrentUser(String type);

}
