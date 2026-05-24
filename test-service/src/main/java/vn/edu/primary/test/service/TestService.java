package vn.edu.primary.test.service;

import vn.edu.primary.test.dto.CreateTestRequest;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.dto.TestResponse;

import java.util.List;

public interface TestService {
    TestResponse createTest(CreateTestRequest request, Long userId, String userName);
    TestResponse getTestById(Long testId, Long userId);
    List<TestResponse> getAllTests(Long userId);
    List<TestResponse> getAllTestsForAdmin();
    TestResponse getTestByIdForAdmin(Long testId);
    TestResponse updateTest(Long testId, CreateTestRequest request, Long userId);
    void deleteTest(Long testId, Long userId);
    void deleteTestForAdmin(Long testId);
    byte[] generateDocx(Long testId, Long userId);
    List<QuestionDTO> getAllQuestionsByUser(Long userId);
    List<QuestionDTO> getFilteredQuestions(Long userId, String filterType, String subject, String lessonContent, String testType);
}
