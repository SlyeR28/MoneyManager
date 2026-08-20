package org.moneymanagement.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneymanagement.Entity.Category;
import org.moneymanagement.Entity.ProfileEntity;
import org.moneymanagement.Mappers.CatogeryMapper;
import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;
import org.moneymanagement.Repository.CategoryRepository;
import org.moneymanagement.Service.ProfileService;
import org.moneymanagement.Service.impl.CategoryServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepo;

    @Mock
    private CatogeryMapper catogeryMapper;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("createCategory should save category and return response when name is unique")
    void testCreateCategory_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).email("user@example.com").build();
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType("income");

        Category category = Category.builder().name("Salary").type("income").build();
        CategoryResponse response = CategoryResponse.builder().id(10L).name("Salary").type("income").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.existsByNameAndProfileId("Salary", 1L)).thenReturn(false);
        when(catogeryMapper.toEntityCategory(request)).thenReturn(category);
        when(categoryRepo.save(any(Category.class))).thenReturn(category);
        when(catogeryMapper.toCategoryResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Salary");
        verify(categoryRepo).save(category);
    }

    @Test
    @DisplayName("createCategory should throw RuntimeException when category name already exists")
    void testCreateCategory_AlreadyExists() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.existsByNameAndProfileId("Salary", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");

        verify(categoryRepo, never()).save(any());
    }

    @Test
    @DisplayName("updateCategory should update fields and save category")
    void testUpdateCategory_Success() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Category existing = Category.builder().id(10L).name("Old Name").type("income").icon("old-icon").build();

        CategoryRequest request = new CategoryRequest();
        request.setName("New Name");
        request.setType("expense");
        request.setIcon("new-icon");

        CategoryResponse response = CategoryResponse.builder().id(10L).name("New Name").type("expense").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.findByIdAndProfileId(10L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.save(existing)).thenReturn(existing);
        when(catogeryMapper.toCategoryResponse(existing)).thenReturn(response);

        CategoryResponse result = categoryService.updateCategory(10L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getType()).isEqualTo("expense");
        assertThat(existing.getIcon()).isEqualTo("new-icon");
        verify(categoryRepo).save(existing);
    }

    @Test
    @DisplayName("updateCategory should throw RuntimeException when category not found")
    void testUpdateCategory_NotFound() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        CategoryRequest request = new CategoryRequest();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.findByIdAndProfileId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category Not Found");
    }

    @Test
    @DisplayName("getCategoiesForCurrentUser should return list of mapped categories")
    void testGetCategoriesForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Category category = Category.builder().id(1L).name("Food").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Food").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.findByProfileId(1L)).thenReturn(List.of(category));
        when(catogeryMapper.toCategoryResponse(category)).thenReturn(response);

        List<CategoryResponse> list = categoryService.getCategoiesForCurrentUser();

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getName()).isEqualTo("Food");
    }

    @Test
    @DisplayName("getCategoiesByTypeForCurrentUser should return list for specified type")
    void testGetCategoriesByTypeForCurrentUser() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        Category category = Category.builder().id(1L).name("Salary").type("income").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Salary").type("income").build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(categoryRepo.findByTypeAndProfileId("income", 1L)).thenReturn(List.of(category));
        when(catogeryMapper.toCategoryResponse(category)).thenReturn(response);

        List<CategoryResponse> list = categoryService.getCategoiesByTypeForCurrentUser("income");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getType()).isEqualTo("income");
    }
}
