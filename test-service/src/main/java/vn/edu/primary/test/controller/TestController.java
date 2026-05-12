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
        try {
            log.info("Fetching all tests");
            List<TestResponse> tests = testService.getAllTestsForAdmin();
            return ResponseEntity.ok(ApiResponse.success("Tests fetched successfully", tests));
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

    private Long extractUserIdFromToken(String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            return userInfo.getId();
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất userId từ token: {}", e.getMessage());
            return 1L;
        }
    }

    private String extractUsernameFromToken(String token) {
        try {
            UserInfo userInfo = resolveUserInfo(token);
            return userInfo.getUsername();
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất username từ token: {}", e.getMessage());
            return "Unknown";
        }
    }

    private UserInfo resolveUserInfo(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            UserInfo userInfo = fetchUserInfoFromGateway(token);
            if (userInfo != null && userInfo.getId() != null && userInfo.getUsername() != null) {
                return userInfo;
            }

            String jwt = token.substring(7);
            try {
                if (jwtProvider.validateToken(jwt)) {
                    String username = jwtProvider.extractUsername(jwt);
                    Long userId = jwtProvider.extractUserId(jwt);
                    if (username != null && !username.isEmpty() && userId != null) {
                        return new UserInfo(userId, username);
                    }
                }
            } catch (Exception e) {
                log.warn("Không thể validate token bằng JwtProvider local: {}", e.getMessage());
            }
        }
        log.warn("Không lấy được user info, sử dụng userId mặc định: 1 và username mặc định: Unknown");
        return new UserInfo(1L, "Unknown");
    }

    private UserInfo fetchUserInfoFromGateway(String authorizationHeader) {
        try {
            String url = gatewayApiUrl + "/user/me";
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            var jsonNode = mapper.readTree(response.getBody());
            Long id = jsonNode.get("id").asLong();
            String username = jsonNode.get("username").asText();
            Integer roleId = jsonNode.get("roleId").asInt();

            return new UserInfo(id, username, roleId);
        } catch (HttpClientErrorException e) {
            log.warn("Không lấy được user info từ gateway: {}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi gọi user/me: {}", e.getMessage());
            return null;
        }
    }

    private static class UserInfo {
        private Long id;
        private String username;
        private Integer roleId;

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
