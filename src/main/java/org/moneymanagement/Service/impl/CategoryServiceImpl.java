package org.moneymanagement.Service.impl;

import lombok.AllArgsConstructor;
import org.moneymanagement.Mappers.CatogeryMapper;
import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Service.CategoryService;
import org.moneymanagement.Service.ProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final CatogeryMapper catogeryMapper;
    private final ProfileService profileService;


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        return null;

    }

    @Override
    public CategoryResponse updateCategory(CategoryRequest categoryRequest) {
        return null;
    }

    @Override
    public void deleteCategory(Long id) {

    }

    @Override
    public CategoryResponse findById(Long id) {
        return null;
    }

    @Override
    public List<CategoryResponse> findAll() {
        return List.of();
    }
}
