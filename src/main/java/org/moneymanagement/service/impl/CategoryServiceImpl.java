package org.moneymanagement.service.impl;

import lombok.AllArgsConstructor;
import org.moneymanagement.entity.Category;
import org.moneymanagement.entity.ProfileEntity;
import org.moneymanagement.mappers.CatogeryMapper;
import org.moneymanagement.payload.request.CategoryRequest;
import org.moneymanagement.payload.response.CategoryResponse;
import org.moneymanagement.repository.CategoryRepository;
import org.moneymanagement.service.CategoryService;
import org.moneymanagement.service.ProfileService;
import org.moneymanagement.exception.DuplicateResourceException;
import org.moneymanagement.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_NOT_FOUND = "Category Not Found with id: ";

    private final CategoryRepository categoryRepo;
    private final CatogeryMapper catogeryMapper;
    private final ProfileService profileService;


    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        if(Boolean.TRUE.equals(categoryRepo.existsByNameAndProfileId(categoryRequest.getName(), currentProfile.getId()))){
            throw new DuplicateResourceException("Category with name " + categoryRequest.getName() + " already exists");
        }
        Category category = catogeryMapper.toEntityCategory(categoryRequest);
        categoryRepo.save(category);
        return catogeryMapper.toCategoryResponse(category);

    }

    @Override
    public CategoryResponse updateCategory(Long categoryId ,CategoryRequest categoryRequest) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepo.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + categoryId));
        category.setName(categoryRequest.getName());
        category.setIcon(categoryRequest.getIcon());
        category.setType(categoryRequest.getType());
        categoryRepo.save(category);

        return catogeryMapper.toCategoryResponse(category);
    }

    @Override
    public void deleteCategory(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepo.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + id));
        categoryRepo.delete(category);
    }

    @Override
    public CategoryResponse findById(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Category category = categoryRepo.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND + id));
        return catogeryMapper.toCategoryResponse(category);
    }


    @Override
    public List<CategoryResponse> getCategoiesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<Category> byProfileId = categoryRepo.findByProfileId(currentProfile.getId());
        return byProfileId.stream()
                .map(catogeryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getCategoiesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<Category> profileId = categoryRepo.findByTypeAndProfileId(type, profile.getId());
        return profileId.stream().map(catogeryMapper::toCategoryResponse).toList();
    }
}
