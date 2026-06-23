package vn.edu.primary.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.edu.primary.test.dto.ApiResponse;
import vn.edu.primary.test.repository.TestAttemptRepository;
import vn.edu.primary.test.repository.TestRepository;
import vn.edu.primary.test.dto.CreateTestRequest;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.dto.TestResponse;
import vn.edu.primary.test.dto.AttemptStatisticsDTO;
import vn.edu.primary.test.dto.LessonContentDto;
import vn.edu.primary.test.security.JwtProvider;
import vn.edu.primary.test.service.LessonContentService;
import vn.edu.primary.test.service.TestService;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestService testService;
    private final LessonContentService lessonContentService;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final TestAttemptRepository testAttemptRepository;
    private final TestRepository testRepository;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url:http://localhost:8001}")
    private String pythonApiUrl;

    @Value("${gateway.api.url:http://localhost:8080/api}")
    private String gatewayApiUrl;

    @Value("${speech.recognition.api.url:http://localhost:8086/api/pronunciation}")
    private String speechRecognitionApiUrl;

    @Value("${user.service.url:http://localhost:8082/api}")
    private String userServiceUrl;

    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(
            @RequestBody CreateTestRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Creating test: {}", request.getName());
            Long userId = request.getUserId();
            String userName = request.getUserName();
            
            if (userId == null || userId <= 0 || userName == null || userName.isBlank()) {
                userId = extractUserIdFromToken(token);
                userName = extractUsernameFromToken(token);
            }

            if (token != null && !token.trim().isEmpty() && request.getTestType() != null) {
                UserInfo userInfo = resolveUserInfo(token);
                if (userInfo.getRoleId() != null && userInfo.getRoleId() == 1 && "EXERCISE".equalsIgnoreCase(request.getTestType())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Học sinh không được phép tạo bài tập"));
                }
            }
            
            TestResponse response = testService.createTest(request, userId, userName);
            return ResponseEntity.ok(ApiResponse.success("Test created successfully", response));
        } catch (Exception e) {
            log.error("Error creating test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating test: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TestResponse>>> getAllTests(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authorization token is required"));
        }
        try {
            log.info("Fetching all tests for current user");
            log.info("Authorization header present, length={}", token.length());
            Long userId = extractUserIdFromToken(token);
            log.info("Resolved userId from token: {}", userId);
            List<TestResponse> tests = testService.getAllTests(userId);
            return ResponseEntity.ok(ApiResponse.success("Tests fetched successfully", tests));
        } catch (RuntimeException e) {
            log.error("Authentication error fetching tests", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching tests", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching tests: " + e.getMessage()));
        }
    }

    @GetMapping("/questions/user")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getAllQuestionsByUser(
            @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authorization token is required"));
        }
        try {
            log.info("Fetching all questions for current user");
            Long userId = extractUserIdFromToken(token);
            List<QuestionDTO> questions = testService.getAllQuestionsByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("Questions fetched successfully", questions));
        } catch (RuntimeException e) {
            log.error("Authentication error fetching questions", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching questions", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching questions: " + e.getMessage()));
        }
    }

    @GetMapping("/questions/filter")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getFilteredQuestions(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String lessonContent,
            @RequestParam(required = false) String testType) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authorization token is required"));
        }
        try {
            log.info("Fetching filtered questions: filterType={}, subject={}, lessonContent={}", filterType, subject, lessonContent);
            Long userId = extractUserIdFromToken(token);
            List<QuestionDTO> questions = testService.getFilteredQuestions(userId, filterType, subject, lessonContent, testType);
            return ResponseEntity.ok(ApiResponse.success("Questions fetched successfully", questions));
        } catch (RuntimeException e) {
            log.error("Authentication error fetching questions", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching questions", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching questions: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<TestResponse>>> getAllTestsForAdmin() {
        try {
            log.info("Fetching all tests for admin");
            List<TestResponse> tests = testService.getAllTestsForAdmin();
            return ResponseEntity.ok(ApiResponse.success("Tests fetched successfully", tests));
        } catch (Exception e) {
            log.error("Error fetching tests for admin", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching tests: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> getTestById(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Fetching test: {}", testId);
            Long userId = extractUserIdFromToken(token);
            TestResponse test = testService.getTestById(testId, userId);
            return ResponseEntity.ok(ApiResponse.success("Test fetched successfully", test));
        } catch (Exception e) {
            log.error("Error fetching test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching test: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}/attempts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttempts(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Fetching attempts for test: {}", testId);
            Long userId = null;
            UserInfo userInfo = null;
            try {
                userInfo = resolveUserInfo(token);
                if (userInfo != null) {
                    userId = userInfo.getId();
                    log.info("✓ Resolved user: id={}, username={}, role={}", userId, userInfo.getUsername(), userInfo.getRole());
                }
            } catch (Exception e) {
                log.warn("✗ Could not resolve user info: {}", e.getMessage());
            }

            var testOpt = testRepository.findById(testId);
            Map<String, Object> response = new HashMap<>();
            
            if (testOpt.isPresent()) {
                var test = testOpt.get();
                if (test.getStartAt() != null && LocalDateTime.now().isBefore(test.getStartAt())) {
                    log.info("→ Test not yet available, returning empty attempts");
                    response.put("message", "Chưa tới thời gian làm bài");
                    response.put("isAvailable", false);
                    response.put("startAt", test.getStartAt());
                    response.put("attempts", new ArrayList<>());
                    return ResponseEntity.ok(ApiResponse.success("Attempts fetched", response));
                }
            }

            List<vn.edu.primary.test.entity.TestAttempt> attempts;
            boolean isTeacher = userInfo != null && userInfo.getRole() != null && 
                    (userInfo.getRole().equalsIgnoreCase("TEACHER") || userInfo.getRole().equalsIgnoreCase("ADMIN"));
            
            if (isTeacher) {
                attempts = testAttemptRepository.findByTest_IdOrderByCreatedAtDesc(testId);
                log.info("→ Teacher request: returning all attempts count={}", attempts.size());
            } else if (userId != null) {
                attempts = testAttemptRepository.findByTest_IdAndUserIdOrderByCreatedAtDesc(testId, userId);
                log.info("→ Student request: returning attempts for userId={}, count={}", userId, attempts.size());
            } else {
                attempts = new ArrayList<>();
                log.info("→ No user info, returning empty attempts");
            }
            
            List<Map<String, Object>> attemptsList = new ArrayList<>();

            for (vn.edu.primary.test.entity.TestAttempt a : attempts) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("userId", a.getUserId());
                m.put("userName", a.getUserName());
                m.put("startedAt", a.getStartedAt());
                m.put("submittedAt", a.getSubmittedAt());
                m.put("durationMinutes", a.getDurationMinutes());
                m.put("durationSeconds", a.getDurationSeconds());
                m.put("score", a.getScore() == null ? 0 : a.getScore());
                m.put("maxScore", a.getMaxScore() == null ? 0 : a.getMaxScore());
                m.put("isSubmitted", Boolean.TRUE.equals(a.getIsSubmitted()));
                if (a.getAnswersJson() != null) {
                    try {
                        Object answersData = objectMapper.readValue(a.getAnswersJson(), new TypeReference<Object>() {});
                        m.put("answersJson", answersData);
                    } catch (Exception ignore) {
                        m.put("answersJson", a.getAnswersJson());
                    }
                }
                attemptsList.add(m);
            }

            vn.edu.primary.test.dto.AttemptStatisticsDTO statistics = calculateAttemptStatistics(attempts);
            
            response.put("statistics", statistics);
            response.put("attempts", attemptsList);
            response.put("isAvailable", true);
            log.info("✓ Returning attempts response: statistics={}, attempts={}", statistics, attemptsList.size());

            return ResponseEntity.ok(ApiResponse.success("Attempts fetched", response));
        } catch (Exception e) {
            log.error("Error fetching attempts", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching attempts: " + e.getMessage()));
        }
    }

    @PostMapping("/{testId}/submissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitSubmission(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Received submission for test {}: payload keys={}", testId, payload == null ? 0 : payload.keySet());
            try {
                vn.edu.primary.test.entity.TestAttempt attempt = null;

                Long resolvedUserId = null;
                String resolvedUserName = null;
                String userRole = null;
                try {
                    var userInfo = resolveUserInfo(token);
                    resolvedUserId = userInfo.getId();
                    resolvedUserName = userInfo.getUsername();
                    userRole = userInfo.getRole();
                    log.info("✓ Resolved user: id={}, username={}, role={}", resolvedUserId, resolvedUserName, userRole);
                } catch (Exception ignore) {
                    log.warn("✗ Failed to resolve user info from token: {}", ignore.getMessage());
                }

                if (userRole != null && (userRole.equalsIgnoreCase("TEACHER") || userRole.equalsIgnoreCase("ADMIN"))) {
                    log.warn("✗ Rejected: User with role '{}' cannot submit test answers. userId={}", userRole, resolvedUserId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Giáo viên không thể nộp bài kiểm tra. Chỉ xem lịch sử học sinh."));
                }

                if (resolvedUserId != null) {
                    var list = testAttemptRepository.findByTest_IdAndUserIdOrderByCreatedAtDesc(testId, resolvedUserId);
                    if (list != null && !list.isEmpty()) {
                        for (var t : list) {
                            if (t.getIsSubmitted() == null || !t.getIsSubmitted()) {
                                attempt = t;
                                log.info("✓ Found existing unsubmitted attempt: id={}", t.getId());
                                break;
                            }
                        }
                    }
                }

                if (attempt == null) {
                    attempt = vn.edu.primary.test.entity.TestAttempt.builder()
                            .userId(resolvedUserId)
                            .userName(resolvedUserName)
                            .build();
                    log.info("→ Created new attempt with userId={}, userName={}", resolvedUserId, resolvedUserName);
                }

                try {
                    var userInfo = resolveUserInfo(token);
                    attempt.setUserId(userInfo.getId());
                    attempt.setUserName(userInfo.getUsername());
                } catch (Exception ignore) {}

                if (payload != null) {
                    if (payload.get("submittedAt") != null) {
                        LocalDateTime submittedAtTime = parsePayloadDateTime(payload.get("submittedAt"));
                        if (submittedAtTime != null) attempt.setSubmittedAt(submittedAtTime);
                    }
                    if (attempt.getStartedAt() == null && payload.get("startedAt") != null) {
                        LocalDateTime startedAtTime = parsePayloadDateTime(payload.get("startedAt"));
                        if (startedAtTime != null) attempt.setStartedAt(startedAtTime);
                    }
                    try {
                        attempt.setAnswersJson(objectMapper.writeValueAsString(payload.get("answers")));
                    } catch (Exception ignore) {}
                }

                try {
                    var testOpt = testRepository.findById(testId);
                    if (testOpt.isPresent()) {
                        attempt.setTest(testOpt.get());
                        log.info("✓ Set test for attempt: testId={}", testId);
                    } else {
                        log.warn("✗ Test not found: testId={}", testId);
                    }
                } catch (Exception ignore) {
                    log.warn("✗ Error finding test: {}", ignore.getMessage());
                }

                if (attempt.getCreatedAt() == null) attempt.setCreatedAt(java.time.LocalDateTime.now());
                attempt.setUpdatedAt(java.time.LocalDateTime.now());

                attempt.setIsSubmitted(true);
                attempt.setStatus("submitted");

                if (attempt.getSubmittedAt() == null) attempt.setSubmittedAt(java.time.LocalDateTime.now());

                if (attempt.getStartedAt() != null && attempt.getSubmittedAt() != null) {
                    try {
                        Duration d = Duration.between(attempt.getStartedAt(), attempt.getSubmittedAt());
                        long seconds = Math.max(0, d.getSeconds());
                        int secs = (int) seconds;
                        int mins = (int) ((seconds + 59) / 60);
                        attempt.setDurationSeconds(secs);
                        attempt.setDurationMinutes(mins);
                    } catch (Exception ignore) {}
                }

                TestResponse testData = null;
                try {
                    testData = testService.getTestById(testId, resolvedUserId);
                } catch (Exception ignore) {}

                int computedMaxScore = computeMaxScore(testData);
                List<Map<String, Object>> audioEvaluations = new ArrayList<>();
                int computedScore = computeScore(testData, payload == null ? null : payload.get("answers"), audioEvaluations);

                if (payload != null && payload.get("score") != null) {
                    try {
                        attempt.setScore(Integer.parseInt(payload.get("score").toString()));
                    } catch (Exception ignore) {
                        attempt.setScore(computedScore);
                    }
                } else {
                    attempt.setScore(computedScore);
                }

                attempt.setMaxScore(payload != null && payload.get("maxScore") != null
                        ? Integer.parseInt(payload.get("maxScore").toString())
                        : (attempt.getMaxScore() == null ? computedMaxScore : attempt.getMaxScore()));

                attempt = testAttemptRepository.save(attempt);
                log.info("✓ Saved attempt: id={}, userId={}, testId={}, score={}/{}", 
                    attempt.getId(), attempt.getUserId(), testId, attempt.getScore(), attempt.getMaxScore());

                Map<String, Object> result = new HashMap<>();
                result.put("score", attempt.getScore());
                result.put("maxScore", attempt.getMaxScore());
                result.put("status", attempt.getStatus());
                result.put("submittedAt", attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().toString() : java.time.Instant.now().toString());
                result.put("attemptId", attempt.getId());
                result.put("durationMinutes", attempt.getDurationMinutes());
                result.put("durationSeconds", attempt.getDurationSeconds());
                if (!audioEvaluations.isEmpty()) {
                    result.put("audioEvaluations", audioEvaluations);
                }
                return ResponseEntity.ok(ApiResponse.success("Submission received", result));
            } catch (Exception ex) {
                log.warn("Attempt repository not available or error saving attempt: {}", ex.getMessage(), ex);
                Map<String, Object> result = new HashMap<>();
                result.put("score", 0);
                result.put("maxScore", payload != null && payload.get("maxScore") != null ? payload.get("maxScore") : 0);
                result.put("status", "submitted");
                result.put("submittedAt", java.time.Instant.now().toString());
                result.put("durationMinutes", payload != null && payload.get("durationMinutes") != null ? payload.get("durationMinutes") : 0);
                result.put("durationSeconds", payload != null && payload.get("durationSeconds") != null ? payload.get("durationSeconds") : 0);
                return ResponseEntity.ok(ApiResponse.success("Submission received", result));
            }
        } catch (Exception e) {
            log.error("Error receiving submission", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error submitting: " + e.getMessage()));
        }
    }

    @PostMapping("/{testId}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitAlias(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return submitSubmission(testId, payload, token);
    }

    private int computeMaxScore(TestResponse testData) {
        if (testData == null) return 0;
        if (testData.getTotalPoints() != null && testData.getTotalPoints() > 0) {
            return testData.getTotalPoints();
        }
        return testData.getQuestions() == null ? 0 : testData.getQuestions().stream()
                .mapToInt(q -> q.getPoints() == null ? 0 : q.getPoints())
                .sum();
    }

    private int computeScore(TestResponse testData, Object answersObj, List<Map<String, Object>> audioEvaluations) {
        if (testData == null || testData.getQuestions() == null || testData.getQuestions().isEmpty()) {
            return 0;
        }
        Map<String, Object> answers = parseAnswersMap(answersObj);
        log.info("=== SCORING START ===");
        log.info("Test: {} questions", testData.getQuestions().size());
        log.info("Answers received: {}", answers.keySet());
        int totalScore = 0;
        for (QuestionDTO question : testData.getQuestions()) {
            if (question == null) continue;
            Object answer = findAnswerForQuestion(answers, question.getId());
            if (answer == null) {
                log.debug("No answer found for question {}", question.getId());
                continue;
            }
            int questionPoints = question.getPoints() == null ? 0 : question.getPoints();
            if (questionPoints <= 0) questionPoints = 1;
            String type = question.getType() == null ? "MULTIPLE_CHOICE" : question.getType().toString().toUpperCase().replace('-', '_');
            try {
                Map<String, Object> answerMap = null;
                if (answer instanceof Map) {
                    answerMap = (Map<String, Object>) answer;
                } else {
                    try {
                        answerMap = objectMapper.convertValue(answer, new TypeReference<Map<String, Object>>() {});
                    } catch (Exception ignore) {
                        answerMap = null;
                    }
                }
                if ("MULTIPLE_CHOICE".equals(type)) {
                    Integer selectedIndex = null;
                    if (answerMap != null && answerMap.containsKey("selectedIndex")) {
                        selectedIndex = answerMap.get("selectedIndex") == null ? null : objectMapper.convertValue(answerMap.get("selectedIndex"), Integer.class);
                    }
                    log.debug("Q{}: MULTIPLE_CHOICE selectedIndex={}", question.getId(), selectedIndex);
                    if (selectedIndex != null && question.getAnswers() != null && selectedIndex >= 0 && selectedIndex < question.getAnswers().size()) {
                        Boolean isCorrect = question.getAnswers().get(selectedIndex).getIsCorrect();
                        log.debug("Q{}: answer={}, isCorrect={}", question.getId(), question.getAnswers().get(selectedIndex).getLabel(), isCorrect);
                        if (Boolean.TRUE.equals(isCorrect)) {
                            totalScore += questionPoints;
                            log.debug("Q{}: +{} points (total={})", question.getId(), questionPoints, totalScore);
                        } else {
                            log.debug("Q{}: 0 points (isCorrect={} is not true)", question.getId(), isCorrect);
                        }
                    } else {
                        log.debug("Q{}: selectedIndex invalid or answers null", question.getId());
                    }
                } else if ("MATCHING".equals(type)) {
                    List<String> mappings = null;
                    if (answer instanceof List) {
                        mappings = objectMapper.convertValue(answer, new TypeReference<List<String>>() {});
                    } else if (answerMap != null) {
                        Object rawMappings = answerMap.get("mappings");
                        if (rawMappings == null) rawMappings = answerMap.get("answers");
                        if (rawMappings != null) {
                            mappings = objectMapper.convertValue(rawMappings, new TypeReference<List<String>>() {});
                        }
                    }
                    if (question.getMatchingPairs() != null && mappings != null && mappings.size() == question.getMatchingPairs().size()) {
                        int correctCount = 0;
                        for (int i = 0; i < question.getMatchingPairs().size(); i++) {
                            String expected = question.getMatchingPairs().get(i).getRight();
                            String actual = mappings.get(i);
                            if (expected != null && actual != null && expected.trim().equalsIgnoreCase(actual.trim())) {
                                correctCount++;
                            }
                        }
                        int totalPairs = question.getMatchingPairs().size();
                        double partialScore = (double) correctCount / totalPairs * questionPoints;
                        int roundedScore = (int) Math.round(partialScore);
                        totalScore += roundedScore;
                        log.debug("Q{}: MATCHING {}/{} pairs correct, score={} (raw={})", question.getId(), correctCount, totalPairs, roundedScore, partialScore);
                    }
                } else if ("FILL_IN_BLANK".equals(type)) {
                    List<String> submittedAnswers = null;
                    if (answer instanceof List) {
                        submittedAnswers = objectMapper.convertValue(answer, new TypeReference<List<String>>() {});
                    } else if (answerMap != null) {
                        Object rawAnswers = answerMap.get("answers");
                        if (rawAnswers == null) rawAnswers = answerMap.get("values");
                        if (rawAnswers != null) {
                            submittedAnswers = objectMapper.convertValue(rawAnswers, new TypeReference<List<String>>() {});
                        }
                    }
                    if (question.getBlanks() != null && submittedAnswers != null && submittedAnswers.size() >= question.getBlanks().size()) {
                        boolean allCorrect = true;
                        for (int i = 0; i < question.getBlanks().size(); i++) {
                            String expected = question.getBlanks().get(i).getCorrectAnswer();
                            String actual = submittedAnswers.get(i);
                            if (expected == null || actual == null || !expected.trim().equalsIgnoreCase(actual.trim())) {
                                allCorrect = false;
                                break;
                            }
                        }
                        if (allCorrect) totalScore += questionPoints;
                    }
                } else if ("AUDIO".equals(type)) {
                    Map<String, Object> evaluation = evaluateAudioPronunciation(question, answer);
                    if (evaluation != null) {
                        audioEvaluations.add(evaluation);
                        Object scoreAwarded = evaluation.get("scoreAwarded");
                        if (scoreAwarded instanceof Number) {
                            totalScore += ((Number) scoreAwarded).intValue();
                        } else if (scoreAwarded != null) {
                            try {
                                totalScore += Integer.parseInt(scoreAwarded.toString());
                            } catch (NumberFormatException ignore) {
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error scoring question {}: {}", question.getId(), e.getMessage());
            }
        }
        log.info("=== SCORING END: totalScore={} ===", totalScore);
        return totalScore;
    }

    private Map<String, Object> parseAnswersMap(Object answersObj) {
        if (answersObj == null) {
            return new HashMap<>();
        }
        try {
            if (answersObj instanceof Map) {
                Map<?, ?> raw = (Map<?, ?>) answersObj;
                Map<String, Object> normalized = new HashMap<>();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    if (entry.getKey() == null) continue;
                    normalized.put(entry.getKey().toString(), entry.getValue());
                }
                return normalized;
            }
            return objectMapper.convertValue(answersObj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Unable to parse answers map", e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> evaluateAudioPronunciation(QuestionDTO question, Object answerObj) {
        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("questionId", question.getId());
        evaluation.put("questionType", "AUDIO");
        int questionPoints = question.getPoints() == null ? 1 : question.getPoints();
        evaluation.put("pointsPossible", questionPoints);
        evaluation.put("scoreAwarded", 0);
        evaluation.put("passed", false);
        evaluation.put("accuracyScore", null);
        String audioUrl = extractAudioUrl(answerObj);
        evaluation.put("audioUrl", audioUrl);
        if (audioUrl == null || audioUrl.isBlank()) {
            evaluation.put("message", "Không có audio để kiểm tra");
            return evaluation;
        }

        String targetText = null;
        if (question.getTranscript() != null && !question.getTranscript().isBlank()) targetText = question.getTranscript();
        if ((targetText == null || targetText.isBlank()) && question.getPrompt() != null && !question.getPrompt().isBlank()) targetText = question.getPrompt();
        if ((targetText == null || targetText.isBlank()) && question.getContent() != null && !question.getContent().isBlank()) targetText = question.getContent();
        if ((targetText == null || targetText.isBlank()) && question.getAnswers() != null && !question.getAnswers().isEmpty()) {
            for (var ans : question.getAnswers()) {
                try {
                    Boolean isCorrect = ans.getIsCorrect();
                    if (Boolean.TRUE.equals(isCorrect)) {
                        String text = null;
                        try { text = ans.getContent(); } catch (Exception ignore) {}
                        if (text == null || text.isBlank()) {
                            try { text = ans.getLabel(); } catch (Exception ignore) {}
                        }
                        if (text != null && !text.isBlank()) {
                            targetText = text;
                            break;
                        }
                    }
                } catch (Exception ignore) {}
            }
        }
        evaluation.put("targetText", targetText);
        if (targetText == null || targetText.isBlank()) {
            evaluation.put("message", "Không có transcript / nội dung để so sánh");
            return evaluation;
        }

        try {
            byte[] audioBytes = restTemplate.getForObject(audioUrl, byte[].class);
            if (audioBytes == null || audioBytes.length == 0) {
                evaluation.put("message", "Không tải được file audio");
                return evaluation;
            }

            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return "student-audio.wav";
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("target_text", targetText);
            body.add("audio_file", audioResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = speechRecognitionApiUrl.endsWith("/") ? speechRecognitionApiUrl + "check" : speechRecognitionApiUrl + "/check";
            String responseJson = restTemplate.postForObject(url, requestEntity, String.class);
            if (responseJson == null) {
                evaluation.put("message", "Dịch vụ kiểm tra phát âm không trả về dữ liệu");
                return evaluation;
            }

            Map<String, Object> responseMap = objectMapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> data = null;
            if (responseMap.containsKey("data") && responseMap.get("data") instanceof Map) {
                data = (Map<String, Object>) responseMap.get("data");
            } else {
                data = responseMap;
            }
            if (data != null) {
                Object accuracyObj = data.get("accuracy_score");
                String accuracyScore = accuracyObj == null ? null : accuracyObj.toString();
                evaluation.put("accuracyScore", accuracyScore);
                Object recognized = data.get("recognized_text");
                if (recognized != null) {
                    evaluation.put("recognizedText", recognized.toString());
                }
                Object feedback = data.get("feedback");
                if (feedback != null) {
                    evaluation.put("feedback", feedback.toString());
                }
                double accuracyValue = parseAccuracyScore(accuracyScore);
                boolean passed = accuracyValue >= 80.0;
                evaluation.put("passed", passed);
                evaluation.put("scoreAwarded", passed ? questionPoints : 0);
                evaluation.put("message", passed ? "Phát âm đạt full điểm" : "Phát âm chưa đạt, không cộng điểm");
            }
        } catch (Exception e) {
            log.warn("Error checking pronunciation for question {}: {}", question.getId(), e.getMessage(), e);
            evaluation.put("message", "Lỗi khi kiểm tra phát âm: " + e.getMessage());
        }
        return evaluation;
    }

    private String extractAudioUrl(Object answerObj) {
        if (answerObj == null) return null;
        if (answerObj instanceof String) {
            return ((String) answerObj).trim();
        }
        if (answerObj instanceof Map) {
            Map<?, ?> answerMap = (Map<?, ?>) answerObj;
            for (String key : new String[]{"audio", "audioUrl", "url", "secure_url", "src"}) {
                Object value = answerMap.get(key);
                if (value instanceof String && !((String) value).isBlank()) {
                    return ((String) value).trim();
                }
            }
        }
        return null;
    }

    private double parseAccuracyScore(String accuracyScore) {
        if (accuracyScore == null) return 0.0;
        try {
            String cleaned = accuracyScore.replace("%", "").trim();
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private LocalDateTime parsePayloadDateTime(Object value) {
        if (value == null) return null;
        try {
            String raw = value.toString();
            try {
                OffsetDateTime odt = OffsetDateTime.parse(raw);
                return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (DateTimeParseException ex) {
                return LocalDateTime.parse(raw);
            }
        } catch (Exception e) {
            log.warn("Unable to parse date/time payload value: {}", value);
            return null;
        }
    }

    private Object findAnswerForQuestion(Map<String, Object> answers, Long questionId) {
        if (answers == null || questionId == null) return null;
        String idKey = questionId.toString();
        if (answers.containsKey(idKey)) {
            return answers.get(idKey);
        }
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equals(questionId)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @PostMapping("/{testId}/attempts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createAttempt(
            @PathVariable Long testId,
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Create/start attempt for test {}: payload keys={}", testId, payload == null ? 0 : payload.keySet());

            Long resolvedUserId = null;
            String resolvedUserName = null;
            String userRole = null;
            try {
                var userInfo = resolveUserInfo(token);
                resolvedUserId = userInfo.getId();
                resolvedUserName = userInfo.getUsername();
                userRole = userInfo.getRole();
                log.info("→ Resolved user: id={}, username={}, role={}", resolvedUserId, resolvedUserName, userRole);
            } catch (Exception ignore) {}

            if (userRole != null && (userRole.equalsIgnoreCase("TEACHER") || userRole.equalsIgnoreCase("ADMIN"))) {
                log.warn("✗ Rejected: User with role '{}' cannot take tests. userId={}", userRole, resolvedUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Giáo viên không thể làm bài kiểm tra. Chỉ xem lịch sử học sinh."));
            }

            var testOpt = testRepository.findById(testId);
            if (testOpt.isPresent()) {
                var test = testOpt.get();
                if (test.getStartAt() != null && LocalDateTime.now().isBefore(test.getStartAt())) {
                    Map<String, Object> res = new HashMap<>();
                    res.put("message", "Chưa tới thời gian làm bài");
                    res.put("startAt", test.getStartAt());
                    return ResponseEntity.ok(ApiResponse.success("Not available yet", res));
                }
            }

            vn.edu.primary.test.entity.TestAttempt attempt = vn.edu.primary.test.entity.TestAttempt.builder()
                    .userId(resolvedUserId)
                    .userName(resolvedUserName)
                    .build();

            try {
                var to = testRepository.findById(testId);
                if (to.isPresent()) attempt.setTest(to.get());
            } catch (Exception ignore) {}

            attempt.setStartedAt(LocalDateTime.now());
            attempt.setIsSubmitted(false);
            attempt.setStatus("started");
            attempt.setCreatedAt(LocalDateTime.now());
            attempt.setUpdatedAt(LocalDateTime.now());

            attempt = testAttemptRepository.save(attempt);

            Map<String, Object> result = new HashMap<>();
            result.put("attemptId", attempt.getId());
            result.put("startedAt", attempt.getStartedAt());
            result.put("status", attempt.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Attempt started", result));
        } catch (Exception e) {
            log.error("Error creating attempt", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating attempt: " + e.getMessage()));
        }
    }

    @GetMapping("/lesson-contents")
    public ResponseEntity<ApiResponse<List<LessonContentDto>>> getLessonContents() {
        try {
            List<LessonContentDto> contents = lessonContentService.getActiveLessonContents();
            return ResponseEntity.ok(ApiResponse.success("Lesson contents loaded", contents));
        } catch (Exception e) {
            log.error("Error loading lesson contents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error loading lesson contents: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/lesson-contents")
    public ResponseEntity<ApiResponse<List<LessonContentDto>>> adminGetLessonContents(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            validateAdminUser(userInfo);
            List<LessonContentDto> contents = lessonContentService.getAllLessonContents();
            return ResponseEntity.ok(ApiResponse.success("Lesson contents loaded", contents));
        } catch (Exception e) {
            log.error("Error loading admin lesson contents", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error loading admin lesson contents: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/lesson-contents")
    public ResponseEntity<ApiResponse<LessonContentDto>> createLessonContent(
            @RequestBody LessonContentDto request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            validateAdminUser(userInfo);
            LessonContentDto created = lessonContentService.createLessonContent(request, userInfo.getId());
            return ResponseEntity.ok(ApiResponse.success("Lesson content created successfully", created));
        } catch (Exception e) {
            log.error("Error creating lesson content", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error creating lesson content: " + e.getMessage()));
        }
    }

    @PutMapping("/admin/lesson-contents/{contentId}")
    public ResponseEntity<ApiResponse<LessonContentDto>> updateLessonContent(
            @PathVariable Long contentId,
            @RequestBody LessonContentDto request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            validateAdminUser(userInfo);
            LessonContentDto updated = lessonContentService.updateLessonContent(contentId, request, userInfo.getId());
            return ResponseEntity.ok(ApiResponse.success("Lesson content updated successfully", updated));
        } catch (Exception e) {
            log.error("Error updating lesson content", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating lesson content: " + e.getMessage()));
        }
    }

    @DeleteMapping("/admin/lesson-contents/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteLessonContent(
            @PathVariable Long contentId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            validateAdminUser(userInfo);
            lessonContentService.deleteLessonContent(contentId);
            return ResponseEntity.ok(ApiResponse.success("Lesson content deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting lesson content", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error deleting lesson content: " + e.getMessage()));
        }
    }

    private String getCellString(Row row, int colIdx) {
        if (row == null) return null;
        Cell cell = row.getCell(colIdx);
        if (cell == null) return null;
        return cell.toString().trim();
    }

    @PutMapping("/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> updateTest(
            @PathVariable Long testId,
            @RequestBody CreateTestRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Updating test: {}", testId);
            Long userId = extractUserIdFromToken(token);
            TestResponse response = testService.updateTest(testId, request, userId);
            return ResponseEntity.ok(ApiResponse.success("Test updated successfully", response));
        } catch (Exception e) {
            log.error("Error updating test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error updating test: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<ApiResponse<Void>> deleteTest(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Deleting test: {}", testId);
            Long userId = extractUserIdFromToken(token);
            testService.deleteTest(testId, userId);
            return ResponseEntity.ok(ApiResponse.success("Test deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error deleting test: " + e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{testId}")
    public ResponseEntity<ApiResponse<Void>> deleteTestForAdmin(@PathVariable Long testId) {
        try {
            log.info("Deleting test for admin: {}", testId);
            testService.deleteTestForAdmin(testId);
            return ResponseEntity.ok(ApiResponse.success("Test deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error deleting test: " + e.getMessage()));
        }
    }

    @PostMapping("/download/docx")
    public ResponseEntity<?> downloadTestAsDocx(
            @RequestBody CreateTestRequest request,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Downloading test as DOCX without saving");
            
            byte[] docxBytes = generateDocxFromRequest(request);
            
            String filename = (request.getName() != null ? 
                request.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : 
                "test") + ".docx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .body(docxBytes);
        } catch (Exception e) {
            log.error("Error downloading test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error downloading test: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}/download/docx")
    public ResponseEntity<?> downloadTestDocx(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Downloading test {} as DOCX", testId);
            Long userId = extractUserIdFromToken(token);
            TestResponse testResponse = testService.getTestById(testId, userId);
            byte[] docxBytes = testService.generateDocx(testId, userId);
            
            String filename = (testResponse.getName() != null ? 
                testResponse.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : 
                "test") + ".docx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .body(docxBytes);
        } catch (Exception e) {
            log.error("Error downloading test", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error downloading test: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/{testId}/download/docx")
    public ResponseEntity<?> downloadTestDocxForAdmin(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Downloading test {} as DOCX for admin", testId);
            UserInfo userInfo = resolveUserInfo(token);
            if ((userInfo.getRoleId() == null || userInfo.getRoleId() != 3)
                    && (userInfo.getRole() == null || !userInfo.getRole().equalsIgnoreCase("ADMIN"))) {
                throw new RuntimeException("Access denied: admin role required");
            }

            TestResponse testResponse = testService.getTestByIdForAdmin(testId);
            byte[] docxBytes = testService.generateDocx(testId, userInfo.getId());

            String filename = (testResponse.getName() != null ? 
                testResponse.getName().replaceAll("[^a-zA-Z0-9.-]", "_") : 
                "test") + ".docx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    .body(docxBytes);
        } catch (Exception e) {
            log.error("Error downloading test for admin", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error downloading test for admin: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/{testId}")
    public ResponseEntity<ApiResponse<TestResponse>> getTestByIdForAdmin(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Fetching test {} for admin", testId);
            UserInfo userInfo = resolveUserInfo(token);
            if ((userInfo.getRoleId() == null || userInfo.getRoleId() != 3)
                    && (userInfo.getRole() == null || !userInfo.getRole().equalsIgnoreCase("ADMIN"))) {
                throw new RuntimeException("Access denied: admin role required");
            }
            TestResponse testResponse = testService.getTestByIdForAdmin(testId);
            return ResponseEntity.ok(ApiResponse.success("Test fetched successfully", testResponse));
        } catch (Exception e) {
            log.error("Error fetching test for admin", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Error fetching test: " + e.getMessage()));
        }
    }

    private Long extractUserIdFromToken(String token) {
        UserInfo userInfo = resolveUserInfo(token);
        return userInfo.getId();
    }

    private String extractUsernameFromToken(String token) {
        UserInfo userInfo = resolveUserInfo(token);
        return userInfo.getUsername();
    }

    private String resolveJwt(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Authorization header missing or invalid");
        }

        String trimmed = authorizationHeader.trim();
        if (trimmed.toLowerCase().startsWith("bearer ")) {
            trimmed = trimmed.substring(7).trim();
        }

        if (trimmed.isBlank() || "undefined".equalsIgnoreCase(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            throw new RuntimeException("Authorization token missing or invalid");
        }

        return trimmed;
    }

    private String buildAuthorizationHeader(String rawHeader) {
        String jwt = resolveJwt(rawHeader);
        return "Bearer " + jwt;
    }

    private UserInfo resolveUserInfo(String token) {
        String jwt = resolveJwt(token);
        String authorizationHeader = buildAuthorizationHeader(token);

        if (jwtProvider.validateToken(jwt)) {
            String username = jwtProvider.extractUsername(jwt);
            Long userId = jwtProvider.extractUserId(jwt);
            log.info("Local JWT parse result: username={}, userId={}", username, userId);
            if (username != null && !username.isEmpty() && userId != null) {
                return new UserInfo(userId, username);
            }
        } else {
            log.warn("Local JWT validation failed, attempting gateway lookup");
        }

        log.info("Resolving user info from gateway/user-service");
        UserInfo userInfo = fetchUserInfoFromGateway(authorizationHeader);
        if (userInfo != null && userInfo.getId() != null && userInfo.getUsername() != null) {
            log.info("Resolved user info from gateway/user-service: id={}, username={}", userInfo.getId(), userInfo.getUsername());
            return userInfo;
        }

        if (jwtProvider.validateToken(jwt)) {
            String username = jwtProvider.extractUsername(jwt);
            Long userId = jwtProvider.extractUserId(jwt);
            if (username != null && !username.isEmpty()) {
                log.warn("Gateway/user-service lookup failed; returning user info from valid local JWT");
                return new UserInfo(userId, username);
            }
        }

        throw new RuntimeException("Unable to resolve user info from token");
    }

    private void validateAdminUser(UserInfo userInfo) {
        if (userInfo == null) {
            throw new RuntimeException("Unauthorized: unable to resolve user info");
        }
        boolean isAdmin = (userInfo.getRoleId() != null && userInfo.getRoleId() == 3)
                || (userInfo.getRole() != null && userInfo.getRole().equalsIgnoreCase("ADMIN"));
        if (!isAdmin) {
            throw new RuntimeException("Access denied: admin role required");
        }
    }

    private UserInfo fetchUserInfoFromGateway(String authorizationHeader) {
        UserInfo userInfo = tryFetchUserInfo(gatewayApiUrl + "/user/me", authorizationHeader);
        if (userInfo != null) {
            return userInfo;
        }

        log.warn("Gateway lookup failed, trying direct user-service call");
        return tryFetchUserInfo(userServiceUrl + "/user/me", authorizationHeader);
    }

    private UserInfo tryFetchUserInfo(String url, String authorizationHeader) {
        try {
            log.info("Fetching user info from: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            log.info("User info response status: {}", response.getStatusCode());
            String body = response.getBody();
            log.info("Raw user info response body: {}", body);

            ObjectMapper mapper = new ObjectMapper();
            var rootNode = mapper.readTree(body);
            var dataNode = rootNode.has("data") && !rootNode.get("data").isNull()
                    ? rootNode.get("data")
                    : rootNode;

            if (dataNode == null || dataNode.isNull() || !dataNode.has("id") || !dataNode.has("username")) {
                log.warn("User info response missing required fields: {}", body);
                return null;
            }

            Long id = dataNode.get("id").asLong();
            String username = dataNode.get("username").asText();
            Integer roleId = dataNode.has("roleId") && !dataNode.get("roleId").isNull()
                    ? dataNode.get("roleId").asInt()
                    : null;
            String role = dataNode.has("role") && !dataNode.get("role").isNull()
                    ? dataNode.get("role").asText()
                    : null;

            if (roleId == null && role != null) {
                roleId = switch (role.toUpperCase()) {
                    case "ADMIN" -> 3;
                    case "TEACHER" -> 2;
                    case "STUDENT" -> 1;
                    default -> null;
                };
            }

            log.info("Resolved user info: id={}, username={}, roleId={}, role={}", id, username, roleId, role);
            return new UserInfo(id, username, roleId, role);
        } catch (HttpClientErrorException e) {
            log.warn("Cannot fetch user info from {}: {}", url, e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error fetching user info from {}: {}", url, e.getMessage());
            return null;
        }
    }

    private static class UserInfo {
        private Long id;
        private String username;
        private Integer roleId;
        private String role;

        public UserInfo() {}

        public UserInfo(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public UserInfo(Long id, String username, Integer roleId) {
            this.id = id;
            this.username = username;
            this.roleId = roleId;
        }

        public UserInfo(Long id, String username, Integer roleId, String role) {
            this.id = id;
            this.username = username;
            this.roleId = roleId;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(Integer roleId) {
            this.roleId = roleId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    private vn.edu.primary.test.dto.AttemptStatisticsDTO calculateAttemptStatistics(
            List<vn.edu.primary.test.entity.TestAttempt> attempts) {
        if (attempts == null || attempts.isEmpty()) {
            return vn.edu.primary.test.dto.AttemptStatisticsDTO.builder()
                    .totalAttempts(0)
                    .completedAttempts(0)
                    .averageScore(0.0)
                    .averageScorePercentage(0.0)
                    .maxScore(0)
                    .minScore(0)
                    .completionRate(0.0)
                    .build();
        }

        int totalAttempts = attempts.size();
        int completedAttempts = 0;
        int maxScoreValue = 0;
        int minScoreValue = Integer.MAX_VALUE;
        int totalScore = 0;
        int totalMaxScore = 0;

        for (vn.edu.primary.test.entity.TestAttempt attempt : attempts) {
            int score = attempt.getScore() == null ? 0 : attempt.getScore();
            int maxScore = attempt.getMaxScore() == null ? 0 : attempt.getMaxScore();

            totalScore += score;
            totalMaxScore += maxScore;

            if (Boolean.TRUE.equals(attempt.getIsSubmitted())) {
                completedAttempts++;
            }

            if (score > maxScoreValue) {
                maxScoreValue = score;
            }
            if (score < minScoreValue && score >= 0) {
                minScoreValue = score;
            }
        }

        double averageScore = totalAttempts > 0 ? (double) totalScore / totalAttempts : 0.0;
        
        double averageScorePercentage = 0.0;
        if (totalAttempts > 0 && totalMaxScore > 0) {
            averageScorePercentage = (averageScore / (totalMaxScore / (double) totalAttempts)) * 100.0;
        }

        double completionRate = totalAttempts > 0 ? (double) completedAttempts / totalAttempts * 100.0 : 0.0;

        return vn.edu.primary.test.dto.AttemptStatisticsDTO.builder()
                .totalAttempts(totalAttempts)
                .completedAttempts(completedAttempts)
                .averageScore(Math.round(averageScore * 100.0) / 100.0) // Làm tròn 2 chữ số thập phân
                .averageScorePercentage(Math.round(averageScorePercentage * 100.0) / 100.0)
                .maxScore(maxScoreValue)
                .minScore(minScoreValue == Integer.MAX_VALUE ? 0 : minScoreValue)
                .completionRate(Math.round(completionRate * 100.0) / 100.0)
                .build();
    }

    private byte[] generateDocxFromRequest(CreateTestRequest request) {
        try {
            String pythonEndpoint = pythonApiUrl + "/api/docx/generate-test";
            log.info("Calling Python API: {}", pythonEndpoint);
            
            byte[] docxBytes = restTemplate.postForObject(
                    pythonEndpoint,
                    request,
                    byte[].class
            );

            log.info("DOCX generated successfully");
            return docxBytes;
        } catch (Exception e) {
            log.error("Error generating DOCX from request", e);
            throw new RuntimeException("Error generating DOCX: " + e.getMessage());
        }
    }
}
