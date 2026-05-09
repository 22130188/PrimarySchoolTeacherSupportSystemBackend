package vn.edu.primary.test.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.test.dto.AnswerDTO;
import vn.edu.primary.test.dto.CreateTestRequest;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.dto.TestResponse;
import vn.edu.primary.test.entity.Question;
import vn.edu.primary.test.entity.Test;
import vn.edu.primary.test.entity.TestStatus;
import vn.edu.primary.test.repository.QuestionRepository;
import vn.edu.primary.test.repository.TestRepository;
import vn.edu.primary.test.service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url:http://localhost:8001}")
    private String pythonApiUrl;

    @Override
    @Transactional
    public TestResponse createTest(CreateTestRequest request, Long userId, String userName) {
        log.info("Creating test: {} for user: {} ({})", request.getName(), userId, userName);

        Integer totalPoints = request.getQuestions().stream()
                .mapToInt(q -> {
                    if (q.getPoints() == null) return 0;
                    try {
                        return q.getPoints().toString().isEmpty() ? 0 : Integer.parseInt(q.getPoints().toString());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();

        Test test = Test.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .grade(request.getGrade())
                .duration(request.getDuration())
                .description(request.getDescription())
                .createdBy(userId)
                .createdByName(userName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(TestStatus.DRAFT)
                .totalPoints(totalPoints)
                .questionCount(request.getQuestions().size())
                .build();

        Test savedTest = testRepository.save(test);
        log.info("Test created with id: {}", savedTest.getId());

        List<Question> questions = request.getQuestions().stream()
                .map((q) -> Question.builder()
                        .test(savedTest)
                        .type(convertToQuestionType(q))
                        .content(q.getContent() != null ? q.getContent() : "")
                        .points(convertToInteger(q.getPoints()))
                        .title(q.getTitle())
                        .numberQuestions(convertToInteger(q.getNumberQuestions()))
                        .answersJson(convertAnswersToJson(q.getAnswers()))
                        .audioUrl(q.getAudioUrl())
                        .transcript(q.getTranscript())
                        .orderIndex(request.getQuestions().indexOf(q))
                        .build())
                .collect(Collectors.toList());

        questionRepository.saveAll(questions);
        log.info("Saved {} questions for test: {}", questions.size(), savedTest.getId());

        return convertToResponse(savedTest, request.getQuestions());
    }

    @Override
    public TestResponse getTestById(Long testId, Long userId) {
        log.info("Getting test: {} for user: {}", testId, userId);
        Test test = testRepository.findByIdAndCreatedBy(testId, userId)
                .orElseThrow(() -> new RuntimeException("Test not found or access denied"));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(testId);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(this::convertQuestionToDTO)
                .collect(Collectors.toList());

        return convertToResponse(test, questionDTOs);
    }

    @Override
    public List<TestResponse> getAllTests(Long userId) {
        log.info("Getting all tests for user: {}", userId);
        List<Test> tests = testRepository.findByCreatedByOrderByCreatedAtDesc(userId);

        return tests.stream()
                .map(test -> {
                    List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(test.getId());
                    List<QuestionDTO> questionDTOs = questions.stream()
                            .map(this::convertQuestionToDTO)
                            .collect(Collectors.toList());
                    return convertToResponse(test, questionDTOs);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<TestResponse> getAllTestsForAdmin() {
        log.info("Getting all tests for admin");
        List<Test> tests = testRepository.findAll();

        return tests.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // Sort by createdAt desc
                .map(test -> {
                    List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(test.getId());
                    List<QuestionDTO> questionDTOs = questions.stream()
                            .map(this::convertQuestionToDTO)
                            .collect(Collectors.toList());
                    return convertToResponse(test, questionDTOs);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TestResponse updateTest(Long testId, CreateTestRequest request, Long userId) {
        log.info("Updating test: {} for user: {}", testId, userId);
        Test test = testRepository.findByIdAndCreatedBy(testId, userId)
                .orElseThrow(() -> new RuntimeException("Test not found or access denied"));

        test.setName(request.getName());
        test.setSubject(request.getSubject());
        test.setGrade(request.getGrade());
        test.setDuration(request.getDuration());
        test.setDescription(request.getDescription());
        test.setUpdatedAt(LocalDateTime.now());

        Integer totalPoints = request.getQuestions().stream()
                .mapToInt(q -> convertToInteger(q.getPoints()))
                .sum();
        test.setTotalPoints(totalPoints);
        test.setQuestionCount(request.getQuestions().size());

        Test updatedTest = testRepository.save(test);

        questionRepository.deleteAll(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));

        List<Question> questions = request.getQuestions().stream()
                .map((q) -> Question.builder()
                        .test(updatedTest)
                        .type(convertToQuestionType(q))
                        .content(q.getContent() != null ? q.getContent() : "")
                        .points(convertToInteger(q.getPoints()))
                        .title(q.getTitle())
                        .numberQuestions(convertToInteger(q.getNumberQuestions()))
                        .answersJson(convertAnswersToJson(q.getAnswers()))
                        .audioUrl(q.getAudioUrl())
                        .transcript(q.getTranscript())
                        .orderIndex(request.getQuestions().indexOf(q))
                        .build())
                .collect(Collectors.toList());

        questionRepository.saveAll(questions);

        return convertToResponse(updatedTest, request.getQuestions());
    }

    @Override
    @Transactional
    public void deleteTest(Long testId, Long userId) {
        log.info("Deleting test: {} for user: {}", testId, userId);
        Test test = testRepository.findByIdAndCreatedBy(testId, userId)
                .orElseThrow(() -> new RuntimeException("Test not found or access denied"));

        questionRepository.deleteAll(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));
        testRepository.delete(test);
    }

    @Override
    @Transactional
    public void deleteTestForAdmin(Long testId) {
        log.info("Deleting test for admin: {}", testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        questionRepository.deleteAll(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));
        testRepository.delete(test);
    }

    @Override
    public byte[] generateDocx(Long testId, Long userId) {
        log.info("Generating DOCX for test: {} for user: {}", testId, userId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(testId);

        try {
            String pythonEndpoint = pythonApiUrl + "/api/docx/generate-test";
            log.info("Calling Python API: {}", pythonEndpoint);

            CreateTestRequest testRequest = CreateTestRequest.builder()
                    .name(test.getName())
                    .subject(test.getSubject())
                    .duration(test.getDuration())
                    .description(test.getDescription())
                    .questions(questions.stream()
                            .map(this::convertQuestionToDTO)
                            .collect(Collectors.toList()))
                    .build();

            byte[] docxBytes = restTemplate.postForObject(
                    pythonEndpoint,
                    testRequest,
                    byte[].class
            );

            log.info("DOCX generated successfully");
            return docxBytes;
        } catch (Exception e) {
            log.error("Error generating DOCX", e);
            throw new RuntimeException("Error generating DOCX: " + e.getMessage());
        }
    }

    private String convertAnswersToJson(Object answers) {
        try {
            if (answers == null) return null;
            return objectMapper.writeValueAsString(answers);
        } catch (Exception e) {
            log.error("Error converting answers to JSON", e);
            return null;
        }
    }

    private QuestionDTO convertQuestionToDTO(Question question) {
        List<AnswerDTO> answers = null;
        if (question.getAnswersJson() != null && !question.getAnswersJson().isEmpty()) {
            try {
                answers = objectMapper.readValue(question.getAnswersJson(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AnswerDTO.class));
            } catch (Exception e) {
                log.error("Error parsing answers JSON for question {}: {}", question.getId(), e.getMessage());
            }
        }
        
        return QuestionDTO.builder()
                .id(question.getId())
                .type(question.getType().name())  // Convert enum to String for API
                .content(question.getContent())
                .points(question.getPoints())
                .title(question.getTitle())
                .numberQuestions(question.getNumberQuestions())
                .answers(answers)
                .audioUrl(question.getAudioUrl())
                .transcript(question.getTranscript())
                .orderIndex(question.getOrderIndex())
                .build();
    }

    private TestResponse convertToResponse(Test test, List<?> questions) {
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(q -> {
                    if (q instanceof QuestionDTO) {
                        return (QuestionDTO) q;
                    } else if (q instanceof Question) {
                        return convertQuestionToDTO((Question) q);
                    }
                    return null;
                })
                .collect(Collectors.toList());

        return TestResponse.builder()
                .id(test.getId())
                .name(test.getName())
                .subject(test.getSubject())
                .grade(test.getGrade())
                .duration(test.getDuration())
                .createdBy(test.getCreatedBy())
                .createdByName(test.getCreatedByName())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .docxFileUrl(test.getDocxFileUrl())
                .description(test.getDescription())
                .totalPoints(test.getTotalPoints())
                .questionCount(test.getQuestionCount())
                .status(test.getStatus())
                .questions(questionDTOs)
                .build();
    }
    
    private vn.edu.primary.test.entity.QuestionType convertToQuestionType(QuestionDTO q) {
        if (q.getType() == null) {
            return vn.edu.primary.test.entity.QuestionType.MULTIPLE_CHOICE;
        }
        if (q.getType() instanceof vn.edu.primary.test.entity.QuestionType) {
            return (vn.edu.primary.test.entity.QuestionType) q.getType();
        }
        if (q.getType() instanceof String) {
            try {
                return vn.edu.primary.test.entity.QuestionType.valueOf((String) q.getType());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid question type: {}, defaulting to MULTIPLE_CHOICE", q.getType());
                return vn.edu.primary.test.entity.QuestionType.MULTIPLE_CHOICE;
            }
        }
        return vn.edu.primary.test.entity.QuestionType.MULTIPLE_CHOICE;
    }
    
    private Integer convertToInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return 0;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse '{}' to Integer, defaulting to 0", str);
                return 0;
            }
        }
        return 0;
    }
}
