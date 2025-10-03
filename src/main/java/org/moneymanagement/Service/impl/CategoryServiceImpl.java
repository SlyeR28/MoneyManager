package org.moneymanagement.Service.impl;

import lombok.AllArgsConstructor;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.CatogeryMapper;
import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Service.CategoryService;
import org.moneymanagement.Service.ProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepo;
    private final CatogeryMapper catogeryMapper;
    private final ProfileService profileService;


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) throws RuntimeException {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        if(Boolean.TRUE.equals(categoryRepo.existsByNameAndProfileId(categoryRequest.getName(), currentProfile.getId()))){
            throw new RuntimeException("Category with name " + categoryRequest.getName() + " already exists");
        }
        Category category = catogeryMapper.toEntityCategory(categoryRequest);
        categoryRepo.save(category);
        return catogeryMapper.toCategoryResponse(category);

    }

    @Override
    public CategoryResponse updateCategory(Long categoryId ,CategoryRequest categoryRequest) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepo.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
        category.setName(categoryRequest.getName());
        category.setIcon(categoryRequest.getIcon());
        category.setType(categoryRequest.getType());
        categoryRepo.save(category);

        return catogeryMapper.toCategoryResponse(category);
    }

    @Override
    public void deleteCategory(Long id) {

    }

    @Override
    public CategoryResponse findById(Long id) {
        return null;
    }

    @Override
    public List<CategoryResponse> getCategoiesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<Category> byProfileId = categoryRepo.findByProfileId(currentProfile.getId());
        return byProfileId.stream()
                .map(catogeryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoiesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Category> profileId = categoryRepo.findByTypeAndProfileId(type, profile.getId());
        return profileId.stream().map(catogeryMapper::toCategoryResponse).collect(Collectors.toList());
    }
}
