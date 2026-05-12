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
import vn.edu.primary.test.dto.TestResponse;
import vn.edu.primary.test.security.JwtProvider;
import vn.edu.primary.test.service.TestService;

import java.util.List;
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
