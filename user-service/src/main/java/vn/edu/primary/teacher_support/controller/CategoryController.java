package vn.edu.primary.teacher_support.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.CategoryDto;
import vn.edu.primary.teacher_support.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CategoryController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategories(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(categoryService.getCategories(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CategoryDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request, authorization));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CategoryDto request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request, authorization));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
