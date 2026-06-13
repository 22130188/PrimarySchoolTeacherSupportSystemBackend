package vn.edu.primary.test.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.test.dto.LessonContentDto;
import vn.edu.primary.test.entity.LessonContent;
import vn.edu.primary.test.repository.LessonContentRepository;
import vn.edu.primary.test.service.LessonContentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonContentServiceImpl implements LessonContentService {

    private final LessonContentRepository lessonContentRepository;

    @Override
    public List<LessonContentDto> getActiveLessonContents() {
        return lessonContentRepository.findAllByIsActiveTrueOrderBySubjectAscGradeAscNameAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LessonContentDto> getAllLessonContents() {
        return lessonContentRepository.findAllByOrderBySubjectAscGradeAscNameAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LessonContentDto getLessonContentById(Long id) {
        return lessonContentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Lesson content not found: " + id));
    }

    @Override
    @Transactional
    public LessonContentDto createLessonContent(LessonContentDto request, Long userId) {
        LessonContent content = LessonContent.builder()
                .subject(request.getSubject())
                .grade(request.getGrade())
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .createdByUserId(userId)
                .build();

        LessonContent saved = lessonContentRepository.save(content);
        return toDto(saved);
    }

    @Override
    @Transactional
    public LessonContentDto updateLessonContent(Long id, LessonContentDto request, Long userId) {
        LessonContent content = lessonContentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson content not found: " + id));

        content.setSubject(request.getSubject());
        content.setGrade(request.getGrade());
        content.setName(request.getName());
        content.setDescription(request.getDescription());
        content.setIsActive(request.getIsActive() == null ? Boolean.TRUE : request.getIsActive());
        if (userId != null) {
            content.setCreatedByUserId(userId);
        }

        LessonContent updated = lessonContentRepository.save(content);
        return toDto(updated);
    }

    @Override
    @Transactional
    public void deleteLessonContent(Long id) {
        if (!lessonContentRepository.existsById(id)) {
            throw new RuntimeException("Lesson content not found: " + id);
        }
        lessonContentRepository.deleteById(id);
    }

    private LessonContentDto toDto(LessonContent entity) {
        return LessonContentDto.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .grade(entity.getGrade())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdByUserId(entity.getCreatedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
