package vn.edu.primary.test.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.test.dto.AnswerDTO;
import vn.edu.primary.test.dto.BlankDTO;
import vn.edu.primary.test.dto.CreateTestRequest;
import vn.edu.primary.test.dto.MatchingPairDTO;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.dto.TestResponse;
import vn.edu.primary.test.entity.Question;
import vn.edu.primary.test.entity.QuestionType;
import vn.edu.primary.test.entity.Test;
import vn.edu.primary.test.entity.TestStatus;
import vn.edu.primary.test.entity.TestType;
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
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Test name is required");
        }

        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }

        if (request.getDuration() == null) {
            throw new IllegalArgumentException("Duration (minutes) is required");
        }

        List<QuestionDTO> questionsList = request.getQuestions() == null ? java.util.Collections.<QuestionDTO>emptyList() : request.getQuestions();

        Integer totalPoints = questionsList.stream()
                .mapToInt(q -> {
                    if (q == null || q.getPoints() == null) return 0;
                    try {
                        return q.getPoints().toString().isEmpty() ? 0 : Integer.parseInt(q.getPoints().toString());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();

        TestStatus testStatus = parseStatus(request.getStatus(), TestStatus.DRAFT);
        Test test = Test.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .grade(request.getGrade())
                .lessonContentName(request.getLessonContentName())
                .duration(request.getDuration())
                .description(request.getDescription())
                .createdBy(userId)
                .createdByName(userName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .testType(parseTestType(request.getTestType(), TestType.EXAM))
                .status(testStatus)
                .totalPoints(totalPoints)
                .questionCount(questionsList.size())
                .build();

        Test savedTest = testRepository.save(test);
        log.info("Test created with id: {}", savedTest.getId());

        for (QuestionDTO q : questionsList) {
            QuestionType qType = q.getQuestionType();
            if (qType == QuestionType.MATCHING) {
                validateMatchingPairs(q);
            }
        }

        List<Question> questions = questionsList.stream()
                .map((q) -> Question.builder()
                        .test(savedTest)
                        .type(convertToQuestionType(q))
                        .content(q.getContent() != null ? q.getContent() : "")
                        .points(convertToInteger(q.getPoints()))
                        .title(q.getTitle())
                        .numberQuestions(convertToInteger(q.getNumberQuestions()))
                        .answersJson(convertAnswersToJson(q.getAnswers()))
                        .matchingPairsJson(convertAnswersToJson(q.getMatchingPairs()))
                        .textWithBlanks(q.getTextWithBlanks())
                        .blanksJson(convertAnswersToJson(q.getBlanks()))
                        .prompt(q.getPrompt())
                        .maxLength(convertToInteger(q.getMaxLength()))
                        .rubric(q.getRubric())
                        .audioUrl(q.getAudioUrl())
                        .imageUrl(q.getImageUrl())
                        .transcript(q.getTranscript())
                        .orderIndex(request.getQuestions().indexOf(q))
                        .createdBy(userId)
                        .createdByName(userName)
                        .isShared(false)
                        .build())
                .collect(Collectors.toList());

        questionRepository.saveAll(questions);
        log.info("Saved {} questions for test: {}", questions.size(), savedTest.getId());

        return convertToResponse(savedTest, questionsList);
    }

    @Override
    public TestResponse getTestById(Long testId, Long userId) {
        log.info("Getting test: {} for user: {}", testId, userId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(testId);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(this::convertQuestionToDTO)
                .collect(Collectors.toList());

        return convertToResponse(test, questionDTOs);
    }

    @Override
    public TestResponse getTestByIdForAdmin(Long testId) {
        log.info("Getting test for admin: {}", testId);
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

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
        test.setLessonContentName(request.getLessonContentName());
        test.setDuration(request.getDuration());
        test.setDescription(request.getDescription());
        test.setUpdatedAt(LocalDateTime.now());
        test.setStartAt(request.getStartAt());
        test.setEndAt(request.getEndAt());
        if (request.getTestType() != null) {
            test.setTestType(parseTestType(request.getTestType(), test.getTestType() != null ? test.getTestType() : TestType.EXAM));
        }
        if (request.getStatus() != null) {
            test.setStatus(parseStatus(request.getStatus(), test.getStatus()));
        }

        Integer totalPoints = request.getQuestions().stream()
                .mapToInt(q -> convertToInteger(q.getPoints()))
                .sum();
        test.setTotalPoints(totalPoints);
        test.setQuestionCount(request.getQuestions().size());

        Test updatedTest = testRepository.save(test);

        questionRepository.deleteAll(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));

        for (QuestionDTO q : request.getQuestions()) {
            QuestionType qType = q.getQuestionType();
            if (qType == QuestionType.MATCHING) {
                validateMatchingPairs(q);
            }
        }

        List<Question> questions = request.getQuestions().stream()
                .map((q) -> Question.builder()
                        .test(updatedTest)
                        .type(convertToQuestionType(q))
                        .content(q.getContent() != null ? q.getContent() : "")
                        .points(convertToInteger(q.getPoints()))
                        .title(q.getTitle())
                        .numberQuestions(convertToInteger(q.getNumberQuestions()))
                        .answersJson(convertAnswersToJson(q.getAnswers()))
                        .matchingPairsJson(convertAnswersToJson(q.getMatchingPairs()))
                        .textWithBlanks(q.getTextWithBlanks())
                        .blanksJson(convertAnswersToJson(q.getBlanks()))
                        .prompt(q.getPrompt())
                        .maxLength(convertToInteger(q.getMaxLength()))
                        .rubric(q.getRubric())
                        .audioUrl(q.getAudioUrl())
                        .imageUrl(q.getImageUrl())
                        .transcript(q.getTranscript())
                        .orderIndex(request.getQuestions().indexOf(q))
                        .createdBy(userId)
                        .createdByName(updatedTest.getCreatedByName())
                        .isShared(Boolean.TRUE.equals(q.getIsShared()))
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
                    .lessonContentName(test.getLessonContentName())
                    .duration(test.getDuration())
                    .description(test.getDescription())
                    .testType(test.getTestType() != null ? test.getTestType().name() : null)
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
        List<MatchingPairDTO> matchingPairs = null;
        List<BlankDTO> blanks = null;

        if (question.getAnswersJson() != null && !question.getAnswersJson().isEmpty()) {
            try {
                answers = objectMapper.readValue(question.getAnswersJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AnswerDTO.class));
            } catch (Exception e) {
                log.error("Error parsing answers JSON for question {}: {}", question.getId(), e.getMessage());
            }
        }

        if (question.getMatchingPairsJson() != null && !question.getMatchingPairsJson().isEmpty()) {
            try {
                matchingPairs = objectMapper.readValue(question.getMatchingPairsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchingPairDTO.class));
            } catch (Exception e) {
                log.error("Error parsing matching pairs JSON for question {}: {}", question.getId(), e.getMessage());
            }
        }

        if (question.getBlanksJson() != null && !question.getBlanksJson().isEmpty()) {
            try {
                blanks = objectMapper.readValue(question.getBlanksJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, BlankDTO.class));
            } catch (Exception e) {
                log.error("Error parsing blanks JSON for question {}: {}", question.getId(), e.getMessage());
            }
        }
        
        return QuestionDTO.builder()
                .id(question.getId())
                .type(question.getType().name())  
                .content(question.getContent())
                .points(question.getPoints())
                .title(question.getTitle())
                .numberQuestions(question.getNumberQuestions())
                .answers(answers)
                .matchingPairs(matchingPairs)
                .textWithBlanks(question.getTextWithBlanks())
                .blanks(blanks)
                .prompt(question.getPrompt())
                .maxLength(question.getMaxLength())
                .rubric(question.getRubric())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
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
                .lessonContentName(test.getLessonContentName())
                .totalPoints(test.getTotalPoints())
                .questionCount(test.getQuestionCount())
                .testType(test.getTestType() != null ? test.getTestType().name() : null)
                .status(test.getStatus())
                .questions(questionDTOs)
                .build();
    }
    
    private vn.edu.primary.test.entity.QuestionType convertToQuestionType(QuestionDTO q) {
        if (q == null || q.getType() == null) {
            return vn.edu.primary.test.entity.QuestionType.MULTIPLE_CHOICE;
        }
        if (q.getType() instanceof vn.edu.primary.test.entity.QuestionType) {
            return (vn.edu.primary.test.entity.QuestionType) q.getType();
        }
        if (q.getType() instanceof String) {
            String typeString = ((String) q.getType()).trim();
            if (typeString.isEmpty()) {
                return vn.edu.primary.test.entity.QuestionType.MULTIPLE_CHOICE;
            }
            typeString = typeString.toUpperCase().replace('-', '_').replace(' ', '_');
            try {
                return vn.edu.primary.test.entity.QuestionType.valueOf(typeString);
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

    private TestType parseTestType(String testType, TestType defaultType) {
        if (testType == null || testType.isBlank()) {
            return defaultType;
        }
        try {
            return TestType.valueOf(testType.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid test type '{}', defaulting to {}", testType, defaultType);
            return defaultType;
        }
    }

    private TestStatus parseStatus(String status, TestStatus defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }

        try {
            return TestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status '{}', using default {}", status, defaultStatus);
            return defaultStatus;
        }
    }

    @Override
    public List<QuestionDTO> getAllQuestionsByUser(Long userId) {
        log.info("Getting all questions for user: {}", userId);
        
        List<Question> questions = questionRepository.findByTest_CreatedByOrderByIdDesc(userId);
        
        return questions.stream().map(this::convertToQuestionDTO).collect(Collectors.toList());
    }
    
    private QuestionDTO convertToQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setType(question.getType().name());
        dto.setContent(question.getContent());
        dto.setPoints(question.getPoints());
        dto.setTitle(question.getTitle());
        dto.setAudioUrl(question.getAudioUrl());
        dto.setImageUrl(question.getImageUrl());
        dto.setTranscript(question.getTranscript());
        dto.setCreatedBy(question.getCreatedBy());
        dto.setCreatedByName(question.getCreatedByName());
        dto.setIsShared(Boolean.TRUE.equals(question.getIsShared()));
        dto.setCreatedAt(question.getCreatedAt() != null
                ? question.getCreatedAt()
                : question.getTest() != null ? question.getTest().getCreatedAt() : null);
        dto.setUpdatedAt(question.getUpdatedAt());
        
        if (question.getTest() != null) {
            dto.setLessonContentName(question.getTest().getLessonContentName());
            dto.setSubject(question.getTest().getSubject());
            dto.setCreatedByName(question.getTest().getCreatedByName());
            dto.setCreatedBy(question.getTest().getCreatedBy());
            if (question.getTest().getTestType() != null) {
                dto.setTestType(question.getTest().getTestType().name());
            }
        }
        
        try {
            if (question.getAnswersJson() != null) {
                List<AnswerDTO> answers = objectMapper.readValue(
                    question.getAnswersJson(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AnswerDTO.class)
                );
                dto.setAnswers(answers);
            }
            
            if (question.getMatchingPairsJson() != null) {
                List<MatchingPairDTO> pairs = objectMapper.readValue(
                    question.getMatchingPairsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, MatchingPairDTO.class)
                );
                dto.setMatchingPairs(pairs);
            }
            
            if (question.getBlanksJson() != null) {
                List<BlankDTO> blanks = objectMapper.readValue(
                    question.getBlanksJson(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, BlankDTO.class)
                );
                dto.setBlanks(blanks);
            }
            
            dto.setTextWithBlanks(question.getTextWithBlanks());
            dto.setPrompt(question.getPrompt());
            dto.setMaxLength(question.getMaxLength());
            
        } catch (Exception e) {
            log.error("Error parsing JSON for question {}", question.getId(), e);
        }
        
        return dto;
    }

    @Override
    public List<QuestionDTO> getFilteredQuestions(Long userId, String filterType, String subject, String lessonContent, String testType) {
        log.info("Getting filtered questions for user: {} with filterType: {}, subject: {}, lessonContent: {}, testType: {}", 
                userId, filterType, subject, lessonContent, testType);
        System.out.println("=== DEBUG: getFilteredQuestions START ===");
        System.out.println("userId: " + userId);
        System.out.println("filterType: " + filterType);
        System.out.println("subject: " + subject);
        System.out.println("lessonContent: " + lessonContent);
        System.out.println("testType: " + testType);
        
        List<Question> questions;
        
        if ("my-questions".equals(filterType)) {
            System.out.println("Fetching MY OWN questions where createdBy = " + userId);
            questions = questionRepository.findByCreatedByOrderByCreatedAtDesc(userId);
            System.out.println("Found " + questions.size() + " questions created by user");
        } else if ("other-questions".equals(filterType)) {
            System.out.println("Fetching SHARED questions from OTHER users (isShared=true, createdBy != " + userId + ")");
            questions = questionRepository.findSharedQuestionsFromOthersOrderByCreatedAtDesc(userId);
            System.out.println("Found " + questions.size() + " shared questions from others");
        } else {
            System.out.println("Fetching ALL questions (own + shared)");
            questions = questionRepository.findOwnOrSharedQuestionsOrderByCreatedAtDesc(userId);
            System.out.println("Found " + questions.size() + " total questions");
        }
        
        System.out.println("Questions before filtering: " + questions.size());
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("  Question " + i + ": id=" + q.getId() + ", createdBy=" + q.getCreatedBy() + 
                    ", isShared=" + q.getIsShared() + ", testId=" + (q.getTest() != null ? q.getTest().getId() : "null"));
        }
        
        List<QuestionDTO> result = questions.stream()
                .filter(q -> {
                    boolean passFilter = true;
                    String filterReason = "";
                    
                    if (subject != null && !subject.isEmpty() && q.getTest() != null) {
                        if (!subject.equalsIgnoreCase(q.getTest().getSubject())) {
                            passFilter = false;
                            filterReason += "subject mismatch (" + q.getTest().getSubject() + " != " + subject + ") ";
                        }
                    }
                    if (lessonContent != null && !lessonContent.isEmpty() && q.getTest() != null) {
                        if (!lessonContent.equalsIgnoreCase(q.getTest().getLessonContentName())) {
                            passFilter = false;
                            filterReason += "lesson mismatch (" + q.getTest().getLessonContentName() + " != " + lessonContent + ") ";
                        }
                    }
                    if (testType != null && !testType.isEmpty() && q.getTest() != null && q.getTest().getTestType() != null) {
                        if (!testType.equalsIgnoreCase(q.getTest().getTestType().name())) {
                            passFilter = false;
                            filterReason += "testType mismatch (" + q.getTest().getTestType().name() + " != " + testType + ") ";
                        }
                    }
                    
                    if (!passFilter) {
                        System.out.println("  Question " + q.getId() + " filtered out: " + filterReason);
                    }
                    return passFilter;
                })
                .map(this::convertToQuestionDTO)
                .collect(Collectors.toList());
        
        System.out.println("Questions after filtering: " + result.size());
        System.out.println("=== DEBUG: getFilteredQuestions END ===");
        return result;
    }


    private void validateMatchingPairs(QuestionDTO question) {
        if (question.getMatchingPairs() == null || question.getMatchingPairs().isEmpty()) {
            throw new IllegalArgumentException("Matching question must have at least one pair");
        }
        
        for (int i = 0; i < question.getMatchingPairs().size(); i++) {
            MatchingPairDTO pair = question.getMatchingPairs().get(i);
            if (pair.getLeft() == null || pair.getLeft().trim().isEmpty()) {
                throw new IllegalArgumentException("Matching pair " + (i + 1) + " has empty left value");
            }
            if (pair.getRight() == null || pair.getRight().trim().isEmpty()) {
                throw new IllegalArgumentException("Matching pair " + (i + 1) + " has empty right value");
            }
        }
        
        log.info("Validated {} matching pairs", question.getMatchingPairs().size());
    }
}
