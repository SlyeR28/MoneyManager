package org.moneymanagement.Controller;

import lombok.RequiredArgsConstructor;
import org.moneymanagement.Payload.Request.CategoryRequest;
import org.moneymanagement.Payload.Response.CategoryResponse;
import org.moneymanagement.Service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest){
        CategoryResponse category = categoryService.createCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(category);
    }

    @GetMapping("/")
    public ResponseEntity<List<CategoryResponse>>getCategories(){
        List<CategoryResponse> currentUser = categoryService.getCategoiesForCurrentUser();
        return ResponseEntity.ok(currentUser);

    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryResponse>>getCategoriesByType(@PathVariable String type){
        List<CategoryResponse> currentUser = categoryService.getCategoiesByTypeForCurrentUser(type);
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse>getCategoriesByType(@PathVariable long categoryId , @RequestBody CategoryRequest categoryRequest){
        return ResponseEntity.ok( categoryService.updateCategory(categoryId,categoryRequest));
    }


}
