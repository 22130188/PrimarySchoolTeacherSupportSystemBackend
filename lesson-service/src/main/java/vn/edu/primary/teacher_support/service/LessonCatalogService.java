package vn.edu.primary.teacher_support.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.LessonCatalogDto;
import vn.edu.primary.teacher_support.entity.LessonCatalog;
import vn.edu.primary.teacher_support.exception.BusinessException;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.repository.LessonCatalogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonCatalogService {

    private final LessonCatalogRepository catalogRepository;

    public List<LessonCatalogDto> search(String subject, String grade, String volume, String book, boolean activeOnly) {
        return catalogRepository.searchCatalog(blankToNull(subject), normalizeGrade(grade), blankToNull(volume), blankToNull(book), activeOnly)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public LessonCatalogDto create(LessonCatalogDto request, Long userId) {
        validate(request);
        LessonCatalog catalog = LessonCatalog.builder()
                .subject(request.getSubject().trim())
                .grade(normalizeGrade(request.getGrade()))
                .volume(request.getVolume().trim())
                .book(request.getBook().trim())
                .name(request.getName().trim())
                .description(blankToNull(request.getDescription()))
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .createdByUserId(userId)
                .build();
        return toDto(catalogRepository.save(catalog));
    }

    @Transactional
    public LessonCatalogDto update(Long id, LessonCatalogDto request, Long userId) {
        validate(request);
        LessonCatalog catalog = catalogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học trong danh mục"));
        catalog.setSubject(request.getSubject().trim());
        catalog.setGrade(normalizeGrade(request.getGrade()));
        catalog.setVolume(request.getVolume().trim());
        catalog.setBook(request.getBook().trim());
        catalog.setName(request.getName().trim());
        catalog.setDescription(blankToNull(request.getDescription()));
        catalog.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
        if (userId != null) {
            catalog.setCreatedByUserId(userId);
        }
        return toDto(catalogRepository.save(catalog));
    }

    @Transactional
    public void delete(Long id) {
        if (!catalogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bài học trong danh mục");
        }
        catalogRepository.deleteById(id);
    }

    private void validate(LessonCatalogDto request) {
        if (request == null
                || isBlank(request.getSubject())
                || isBlank(request.getGrade())
                || isBlank(request.getVolume())
                || isBlank(request.getBook())
                || isBlank(request.getName())) {
            throw new BusinessException("Vui lòng nhập đầy đủ môn học, lớp, tập, bộ sách và tên bài học");
        }
    }

    private LessonCatalogDto toDto(LessonCatalog entity) {
        return LessonCatalogDto.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .grade(entity.getGrade())
                .volume(entity.getVolume())
                .book(entity.getBook())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdByUserId(entity.getCreatedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalizeGrade(String grade) {
        String value = blankToNull(grade);
        if (value == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(value);
        return matcher.find() ? matcher.group() : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}