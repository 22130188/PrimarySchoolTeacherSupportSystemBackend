package vn.edu.primary.test.service;

import vn.edu.primary.test.dto.LessonContentDto;

import java.util.List;

public interface LessonContentService {
    List<LessonContentDto> getActiveLessonContents();
    List<LessonContentDto> getAllLessonContents();
    LessonContentDto getLessonContentById(Long id);
    LessonContentDto createLessonContent(LessonContentDto request, Long userId);
    LessonContentDto updateLessonContent(Long id, LessonContentDto request, Long userId);
    void deleteLessonContent(Long id);
}
