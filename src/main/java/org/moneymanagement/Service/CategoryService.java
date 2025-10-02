package org.moneymanagement.Service;

import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);
    CategoryResponse updateCategory(CategoryRequest categoryRequest);
    void deleteCategory(Long id);
    CategoryResponse findById(Long id);
    List<CategoryResponse> findAll();

}
