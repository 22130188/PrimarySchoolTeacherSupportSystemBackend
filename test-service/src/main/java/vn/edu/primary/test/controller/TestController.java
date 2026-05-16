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

@RestController
@RequestMapping("/api/tests")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final TestService testService;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;

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
            @RequestParam(required = false) String lessonContent) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authorization token is required"));
        }
        try {
            log.info("Fetching filtered questions: filterType={}, subject={}, lessonContent={}", filterType, subject, lessonContent);
            Long userId = extractUserIdFromToken(token);
            List<QuestionDTO> questions = testService.getFilteredQuestions(userId, filterType, subject, lessonContent);
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

                // Try to detect header row (search first 10 rows)
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
                int nameCol = 3; // default guess

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
                    .body(ApiResponse.error("Error downloading test: " + e.getMessage()));
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
