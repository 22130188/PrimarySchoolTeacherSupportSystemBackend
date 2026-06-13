package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.CategoryDto;
import vn.edu.primary.teacher_support.entity.Category;
import vn.edu.primary.teacher_support.entity.User;
import vn.edu.primary.teacher_support.repository.CategoryRepository;
import vn.edu.primary.teacher_support.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public List<CategoryDto> getCategories(String type) {
        List<Category> categories = (type == null || type.isBlank())
                ? categoryRepository.findAll()
                : categoryRepository.findByTypeOrderByCreatedAtDesc(type);
        return categories.stream().map(this::toDto).toList();
    }

    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        return toDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto request, String authorization) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new RuntimeException("Mã danh mục không được để trống");
        }
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Mã danh mục đã tồn tại");
        }

        Category category = new Category();
        category.setType(request.getType());
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setGrade(request.getGrade());
        category.setSubject(request.getSubject());
        category.setIsActive(request.getIsActive() == null ? true : request.getIsActive());

        User user = findUserFromAuthorization(authorization);
        if (user != null) {
            category.setCreatedBy(user);
        }

        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto request, String authorization) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new RuntimeException("Mã danh mục không được để trống");
        }
        if (categoryRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new RuntimeException("Mã danh mục đã tồn tại");
        }

        category.setType(request.getType());
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setGrade(request.getGrade());
        category.setSubject(request.getSubject());
        category.setIsActive(request.getIsActive() == null ? category.getIsActive() : request.getIsActive());

        User user = findUserFromAuthorization(authorization);
        if (user != null && category.getCreatedBy() == null) {
            category.setCreatedBy(user);
        }

        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy danh mục với ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryDto toDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .type(category.getType())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .grade(category.getGrade())
                .subject(category.getSubject())
                .isActive(category.getIsActive())
                .createdByUsername(category.getCreatedBy() != null ? category.getCreatedBy().getUsername() : null)
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    private User findUserFromAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length());
        if (!jwtService.isValid(token)) {
            return null;
        }

        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username).orElse(null);
    }
}
