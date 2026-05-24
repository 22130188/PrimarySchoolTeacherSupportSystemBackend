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
import vn.edu.primary.test.dto.ApiResponse;
import vn.edu.primary.test.repository.TestAttemptRepository;
import vn.edu.primary.test.repository.TestRepository;
import vn.edu.primary.test.dto.CreateTestRequest;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.dto.TestResponse;
import vn.edu.primary.test.security.JwtProvider;
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
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final TestAttemptRepository testAttemptRepository;
    private final TestRepository testRepository;
    private final ObjectMapper objectMapper;

    @Value("${python.api.url:http://localhost:8001}")
    private String pythonApiUrl;

    @Value("${gateway.api.url:http://localhost:8080/api}")
    private String gatewayApiUrl;

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
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAttempts(
            @PathVariable Long testId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            log.info("Fetching attempts for test: {}", testId);
            Long userId = null;
            try {
                userId = extractUserIdFromToken(token);
            } catch (Exception e) {
            }

            var testOpt = testRepository.findById(testId);
            List<Map<String, Object>> out = new ArrayList<>();
            if (testOpt.isPresent()) {
                var test = testOpt.get();
                if (test.getStartAt() != null && LocalDateTime.now().isBefore(test.getStartAt())) {
                    Map<String, Object> info = new HashMap<>();
                    info.put("message", "Chưa tới thời gian làm bài");
                    info.put("isAvailable", false);
                    info.put("startAt", test.getStartAt());
                    out.add(info);
                    return ResponseEntity.ok(ApiResponse.success("Attempts fetched", out));
                }
            }

            List<vn.edu.primary.test.entity.TestAttempt> attempts = testAttemptRepository.findByTest_IdOrderByCreatedAtDesc(testId);

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
                out.add(m);
            }

            return ResponseEntity.ok(ApiResponse.success("Attempts fetched", out));
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
                try {
                    var userInfo = resolveUserInfo(token);
                    resolvedUserId = userInfo.getId();
                    resolvedUserName = userInfo.getUsername();
                } catch (Exception ignore) {}

                if (resolvedUserId != null) {
                    var list = testAttemptRepository.findByTest_IdAndUserIdOrderByCreatedAtDesc(testId, resolvedUserId);
                    if (list != null && !list.isEmpty()) {
                        for (var t : list) {
                            if (t.getIsSubmitted() == null || !t.getIsSubmitted()) {
                                attempt = t;
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
                    }
                } catch (Exception ignore) {}

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
                int computedScore = computeScore(testData, payload == null ? null : payload.get("answers"));

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

                Map<String, Object> result = new HashMap<>();
                result.put("score", attempt.getScore());
                result.put("maxScore", attempt.getMaxScore());
                result.put("status", attempt.getStatus());
                result.put("submittedAt", attempt.getSubmittedAt() != null ? attempt.getSubmittedAt().toString() : java.time.Instant.now().toString());
                result.put("attemptId", attempt.getId());
                result.put("durationMinutes", attempt.getDurationMinutes());
                result.put("durationSeconds", attempt.getDurationSeconds());
                return ResponseEntity.ok(ApiResponse.success("Submission received", result));
            } catch (Exception ex) {
                log.warn("Attempt repository not available or error saving attempt: {}", ex.getMessage());
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

    private int computeScore(TestResponse testData, Object answersObj) {
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
                        boolean allCorrect = true;
                        for (int i = 0; i < question.getMatchingPairs().size(); i++) {
                            String expected = question.getMatchingPairs().get(i).getRight();
                            String actual = mappings.get(i);
                            if (expected == null || actual == null || !expected.trim().equalsIgnoreCase(actual.trim())) {
                                allCorrect = false;
                                break;
                            }
                        }
                        if (allCorrect) totalScore += questionPoints;
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
            try {
                var userInfo = resolveUserInfo(token);
                resolvedUserId = userInfo.getId();
                resolvedUserName = userInfo.getUsername();
            } catch (Exception ignore) {}

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
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getLessonContents() {
        try {
            Path dbDir = Paths.get("database");
            if (!Files.exists(dbDir) || !Files.isDirectory(dbDir)) {
                return ResponseEntity.ok(ApiResponse.success("No database directory found", new ArrayList<>()));
            }

            Optional<Path> excelFile = Files.list(dbDir)
                    .filter(p -> p.toString().toLowerCase().endsWith(".xlsx") || p.toString().toLowerCase().endsWith(".xls"))
                    .findFirst();

            if (!excelFile.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("No excel file found", new ArrayList<>()));
            }

            List<Map<String, String>> results = new ArrayList<>();

            try (InputStream is = new FileInputStream(excelFile.get().toFile())) {
                Workbook workbook = WorkbookFactory.create(is);
                Sheet sheet = workbook.getSheetAt(0);

                int headerRowIdx = -1;
                int maxHeaderSearch = Math.min(10, sheet.getLastRowNum() + 1);
                for (int r = 0; r < maxHeaderSearch; r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    for (Cell cell : row) {
                        String txt = cell.toString().toLowerCase();
                        if (txt.contains("môn") || txt.contains("mon") || txt.contains("môn học")) {
                            headerRowIdx = r;
                            break;
                        }
                    }
                    if (headerRowIdx >= 0) break;
                }

                int subjectCol = 0;
                int gradeCol = 1;
                int nameCol = 3;

                if (headerRowIdx >= 0) {
                    Row header = sheet.getRow(headerRowIdx);
                    for (Cell cell : header) {
                        String txt = cell.toString().toLowerCase();
                        int c = cell.getColumnIndex();
                        if (txt.contains("môn") || txt.contains("môn học") || txt.contains("mon")) subjectCol = c;
                        if (txt.contains("lớp") || txt.contains("lop") || txt.contains("lớp học")) gradeCol = c;
                        if (txt.contains("bài") || txt.contains("nội dung") || txt.contains("bài học") || txt.contains("nội dung chính")) nameCol = c;
                    }
                }

                int startRow = headerRowIdx >= 0 ? headerRowIdx + 1 : 1;
                for (int r = startRow; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null) continue;
                    String subject = getCellString(row, subjectCol);
                    String grade = getCellString(row, gradeCol);
                    String name = getCellString(row, nameCol);
                    if ((subject == null || subject.isBlank()) && (grade == null || grade.isBlank()) && (name == null || name.isBlank())) continue;
                    Map<String, String> item = new HashMap<>();
                    item.put("subject", subject == null ? "" : subject);
                    item.put("grade", grade == null ? "" : grade);
                    item.put("name", name == null ? "" : name);
                    results.add(item);
                }
            }

            return ResponseEntity.ok(ApiResponse.success("Lesson contents loaded", results));
        } catch (Exception e) {
            log.error("Error loading lesson contents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error reading lesson contents: " + e.getMessage()));
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

    private UserInfo resolveUserInfo(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or invalid");
        }

        log.info("Resolving user info from token");
        UserInfo userInfo = fetchUserInfoFromGateway(token);
        if (userInfo != null && userInfo.getId() != null && userInfo.getUsername() != null) {
            log.info("Resolved user info from gateway: id={}, username={}", userInfo.getId(), userInfo.getUsername());
            return userInfo;
        }

        log.warn("Gateway user/me resolution failed, falling back to local JWT parsing");
        String jwt = token.substring(7);
        if (jwtProvider.validateToken(jwt)) {
            String username = jwtProvider.extractUsername(jwt);
            Long userId = jwtProvider.extractUserId(jwt);
            log.info("Local JWT parse result: username={}, userId={}", username, userId);
            if (username != null && !username.isEmpty()) {
                if (userId != null) {
                    return new UserInfo(userId, username);
                }

                log.info("JWT token did not contain userId claim, resolving by user service using username");
                UserInfo resolvedUserInfo = tryFetchUserInfo(userServiceUrl + "/user/me", token);
                if (resolvedUserInfo != null && resolvedUserInfo.getId() != null && resolvedUserInfo.getUsername() != null) {
                    return resolvedUserInfo;
                }
            }
        } else {
            log.warn("Local JWT validation failed");
        }

        throw new RuntimeException("Unable to resolve user info from token");
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
