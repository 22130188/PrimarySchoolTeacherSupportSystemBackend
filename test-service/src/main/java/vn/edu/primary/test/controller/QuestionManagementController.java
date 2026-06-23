package vn.edu.primary.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vn.edu.primary.test.dto.AnswerDTO;
import vn.edu.primary.test.dto.BlankDTO;
import vn.edu.primary.test.dto.MatchingPairDTO;
import vn.edu.primary.test.dto.QuestionDTO;
import vn.edu.primary.test.entity.Question;
import vn.edu.primary.test.entity.QuestionType;
import vn.edu.primary.test.repository.QuestionRepository;
import vn.edu.primary.test.security.JwtProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionManagementController {

    private final QuestionRepository questionRepository;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${gateway.api.url:http://localhost:8080/api}")
    private String gatewayApiUrl;

    @Value("${user.service.url:http://localhost:8082/api}")
    private String userServiceUrl;


    @PostMapping
    public ResponseEntity<?> createQuestion(
            @RequestBody QuestionDTO questionDTO,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            log.info("[DEBUG] createQuestion called with type: {}, title: {}, content: {}", 
                questionDTO.getType(), questionDTO.getTitle(), questionDTO.getContent());
            
            UserInfo userInfo = extractUserInfoFromToken(authorizationHeader);
            if (userInfo == null || userInfo.getId() == null) {
                log.warn("[DEBUG] UserInfo extraction failed or userId is null");
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Long userId = userInfo.getId();
            log.info("[DEBUG] Creating question for userId: {}", userId);

            Question question = new Question();
            question.setTitle(questionDTO.getTitle());
            question.setContent(questionDTO.getContent());
            question.setPoints(questionDTO.getPoints() != null ? questionDTO.getPoints() : 1);
            question.setPrompt(questionDTO.getPrompt());
            question.setAudioUrl(questionDTO.getAudioUrl());
            question.setImageUrl(questionDTO.getImageUrl());
            question.setTranscript(questionDTO.getTranscript());
            question.setRubric(questionDTO.getRubric());
            question.setMaxLength(questionDTO.getMaxLength());
            question.setCreatedBy(userId);
            question.setCreatedByName(userInfo.getUsername());
            question.setIsShared(questionDTO.getIsShared() != null ? questionDTO.getIsShared() : false);
            question.setOrderIndex(questionDTO.getOrderIndex() != null ? questionDTO.getOrderIndex() : 0);
            
            if (questionDTO.getType() != null) {
                try {
                    String typeStr = questionDTO.getType().toString().toUpperCase();
                    question.setType(QuestionType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid question type: " + questionDTO.getType() + ", defaulting to ESSAY");
                    question.setType(QuestionType.ESSAY);
                }
            } else {
                question.setType(QuestionType.ESSAY);
            }
            
            if (questionDTO.getAnswers() != null) {
                try {
                    question.setAnswersJson(objectMapper.writeValueAsString(questionDTO.getAnswers()));
                } catch (Exception e) {
                    log.warn("Failed to serialize answers", e);
                }
            }

            if (questionDTO.getMatchingPairs() != null) {
                try {
                    question.setMatchingPairsJson(objectMapper.writeValueAsString(questionDTO.getMatchingPairs()));
                } catch (Exception e) {
                    log.warn("Failed to serialize matching pairs", e);
                }
            }

            if (questionDTO.getBlanks() != null) {
                try {
                    question.setBlanksJson(objectMapper.writeValueAsString(questionDTO.getBlanks()));
                } catch (Exception e) {
                    log.warn("Failed to serialize blanks", e);
                }
            }

            if (question.getType() != QuestionType.MULTIPLE_CHOICE) {
                question.setAnswersJson(null);
            }
            if (question.getType() != QuestionType.MATCHING) {
                question.setMatchingPairsJson(null);
            }
            if (question.getType() != QuestionType.FILL_IN_BLANK) {
                question.setBlanksJson(null);
            }

            Question created = questionRepository.save(question);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Question created successfully");
            response.put("question", convertToDTO(created));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[ERROR] Error creating question - Exception: {}, Message: {}", 
                e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to create question",
                "details", e.getMessage(),
                "exceptionType", e.getClass().getSimpleName()
            ));
        }
    }

    @GetMapping("/my-questions")
    public ResponseEntity<?> getMyQuestions(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Question> questions = questionRepository.findByCreatedByOrTestCreatedByOrderByCreatedAtDesc(userId);
            List<QuestionDTO> dtos = questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting user's questions", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get questions"));
        }
    }

   
    @GetMapping("/my-shared")
    public ResponseEntity<?> getMySharedQuestions(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Question> questions = questionRepository.findSharedQuestionsForUserOrderByCreatedAtDesc(userId);
            List<QuestionDTO> dtos = questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting shared questions", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get shared questions"));
        }
    }

    
    @GetMapping("/my-private")
    public ResponseEntity<?> getMyPrivateQuestions(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Question> questions = questionRepository.findPrivateQuestionsForUserOrderByCreatedAtDesc(userId);
            List<QuestionDTO> dtos = questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting private questions", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get private questions"));
        }
    }

    
    @GetMapping("/{questionId}")
    public ResponseEntity<?> getQuestion(@PathVariable Long questionId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Question question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Question not found"));
            }

            if (!question.getCreatedBy().equals(userId) && !question.getIsShared()) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }

            return ResponseEntity.ok(convertToDTO(question));
        } catch (Exception e) {
            log.error("Error getting question", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get question"));
        }
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Long questionId,
            @RequestBody QuestionDTO questionDTO,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            UserInfo userInfo = extractUserInfoFromToken(authorizationHeader);
            if (userInfo == null || userInfo.getId() == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            Long userId = userInfo.getId();

            Question question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Question not found"));
            }

            if (question.getCreatedBy() != null && !question.getCreatedBy().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden - you can only edit your own questions"));
            }

            if (question.getCreatedBy() == null) {
                question.setCreatedBy(userId);
                question.setCreatedByName(userInfo.getUsername());
            }

            question.setTitle(questionDTO.getTitle());
            question.setContent(questionDTO.getContent());
            question.setPoints(questionDTO.getPoints() != null ? questionDTO.getPoints() : question.getPoints() != null ? question.getPoints() : 1);
            question.setPrompt(questionDTO.getPrompt());
            question.setAudioUrl(questionDTO.getAudioUrl());
            question.setImageUrl(questionDTO.getImageUrl());
            question.setTranscript(questionDTO.getTranscript());
            question.setRubric(questionDTO.getRubric());
            question.setMaxLength(questionDTO.getMaxLength());
            question.setIsShared(questionDTO.getIsShared() != null ? questionDTO.getIsShared() : question.getIsShared());
            question.setOrderIndex(questionDTO.getOrderIndex() != null ? questionDTO.getOrderIndex() : question.getOrderIndex() != null ? question.getOrderIndex() : 0);

            if (questionDTO.getType() != null) {
                try {
                    String typeStr = questionDTO.getType().toString().toUpperCase();
                    question.setType(QuestionType.valueOf(typeStr));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid question type: " + questionDTO.getType() + ", keeping current type");
                }
            }

            if (questionDTO.getAnswers() != null) {
                try {
                    question.setAnswersJson(objectMapper.writeValueAsString(questionDTO.getAnswers()));
                } catch (Exception e) {
                    log.warn("Failed to serialize answers", e);
                }
            }

            if (questionDTO.getMatchingPairs() != null) {
                try {
                    question.setMatchingPairsJson(objectMapper.writeValueAsString(questionDTO.getMatchingPairs()));
                } catch (Exception e) {
                    log.warn("Failed to serialize matching pairs", e);
                }
            }

            if (questionDTO.getBlanks() != null) {
                try {
                    question.setBlanksJson(objectMapper.writeValueAsString(questionDTO.getBlanks()));
                } catch (Exception e) {
                    log.warn("Failed to serialize blanks", e);
                }
            }

            Question updated = questionRepository.save(question);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Question updated successfully");
            response.put("question", convertToDTO(updated));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating question", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update question"));
        }
    }


    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Question question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Question not found"));
            }

            if (question.getCreatedBy() != null && !question.getCreatedBy().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden - you can only delete your own questions"));
            }

            questionRepository.delete(question);

            return ResponseEntity.ok(Map.of("message", "Question deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting question", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete question"));
        }
    }

  
    @PatchMapping("/{questionId}/toggle-sharing")
    public ResponseEntity<?> toggleSharing(@PathVariable Long questionId,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            Question question = questionRepository.findById(questionId).orElse(null);
            if (question == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Question not found"));
            }

            boolean isOwner = (question.getCreatedBy() != null && question.getCreatedBy().equals(userId));
            boolean isTestOwner = question.getTest() != null && question.getTest().getCreatedBy() != null && question.getTest().getCreatedBy().equals(userId);
            
            if (question.getCreatedBy() == null) {
                UserInfo userInfo = extractUserInfoFromToken(authorizationHeader);
                if (userInfo != null && userInfo.getId() != null) {
                    question.setCreatedBy(userInfo.getId());
                    question.setCreatedByName(userInfo.getUsername());
                    questionRepository.save(question);
                    isOwner = true;
                }
            }
            
            if (!isOwner && !isTestOwner) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }

            Boolean currentShared = question.getIsShared();
            Boolean isShared = body != null
                    ? body.getOrDefault("isShared", currentShared == null ? Boolean.FALSE : !currentShared)
                    : (currentShared == null ? Boolean.TRUE : !currentShared);
            question.setIsShared(isShared);

            Question updated = questionRepository.save(question);

            Map<String, Object> response = new HashMap<>();
            response.put("message", isShared ? "Question shared successfully" : "Question is now private");
            response.put("isShared", updated.getIsShared());
            response.put("question", convertToDTO(updated));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling sharing status", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to toggle sharing status", "details", e.getMessage()));
        }
    }

    @GetMapping("/shared/all")
    public ResponseEntity<?> getAllSharedQuestions(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            Long userId = extractUserIdFromToken(authorizationHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

            List<Question> questions = questionRepository.findAll().stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsShared()) && (q.getCreatedBy() == null || !q.getCreatedBy().equals(userId)))
                    .sorted((q1, q2) -> {
                        LocalDateTime t1 = q1.getCreatedAt();
                        LocalDateTime t2 = q2.getCreatedAt();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    })
                    .collect(Collectors.toList());

            List<QuestionDTO> dtos = questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Error getting shared questions", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get shared questions"));
        }
    }


    private Long extractUserIdFromToken(String authorizationHeader) {
        try {
            UserInfo userInfo = resolveUserInfo(authorizationHeader);
            return userInfo != null ? userInfo.getId() : null;
        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }

    private UserInfo extractUserInfoFromToken(String authorizationHeader) {
        try {
            return resolveUserInfo(authorizationHeader);
        } catch (Exception e) {
            log.error("Error extracting user info from token", e);
            return null;
        }
    }

    private UserInfo resolveUserInfo(String authorizationHeader) {
        String jwt = resolveJwt(authorizationHeader);
        if (jwt == null) {
            log.warn("No valid JWT found in authorization header");
            return null;
        }

        if (jwtProvider.validateToken(jwt)) {
            String username = jwtProvider.extractUsername(jwt);
            Long userId = jwtProvider.extractUserId(jwt);
            log.info("Local JWT validation successful: username={}, userId={}", username, userId);
            if (username != null && !username.isEmpty() && userId != null) {
                return new UserInfo(userId, username);
            }
        } else {
            log.warn("Local JWT validation failed, attempting gateway lookup");
        }

        UserInfo userInfo = fetchUserInfoFromGateway(authorizationHeader);
        if (userInfo != null && userInfo.getId() != null && userInfo.getUsername() != null) {
            log.info("Resolved user info from gateway: id={}, username={}", userInfo.getId(), userInfo.getUsername());
            return userInfo;
        }

        if (jwtProvider.validateToken(jwt)) {
            String username = jwtProvider.extractUsername(jwt);
            Long userId = jwtProvider.extractUserId(jwt);
            if (username != null && !username.isEmpty()) {
                log.warn("Gateway lookup failed; returning user info from valid local JWT");
                return new UserInfo(userId, username);
            }
        }

        log.error("Unable to resolve user info from token");
        return null;
    }

    private String resolveJwt(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        String trimmed = authorizationHeader.trim();
        if (trimmed.toLowerCase().startsWith("bearer ")) {
            trimmed = trimmed.substring(7).trim();
        }

        if (trimmed.isBlank() || "undefined".equalsIgnoreCase(trimmed) || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }

        return trimmed;
    }

    private String buildAuthorizationHeader(String rawHeader) {
        String jwt = resolveJwt(rawHeader);
        if (jwt == null) {
            return rawHeader;
        }
        return "Bearer " + jwt;
    }

    private UserInfo fetchUserInfoFromGateway(String authorizationHeader) {
        try {
            String authHeader = buildAuthorizationHeader(authorizationHeader);
            
            log.info("Fetching user info from: {}/user/me", gatewayApiUrl);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    gatewayApiUrl + "/user/me",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Long id = null;
                String username = null;
                
                if (body.get("id") != null) {
                    id = ((Number) body.get("id")).longValue();
                }
                if (body.get("username") != null) {
                    username = body.get("username").toString();
                }
                
                if (id != null && username != null) {
                    Integer roleId = null;
                    String role = null;
                    
                    if (body.get("roleId") != null) {
                        roleId = ((Number) body.get("roleId")).intValue();
                    }
                    if (body.get("role") != null) {
                        role = body.get("role").toString();
                    }
                    
                    return new UserInfo(id, username, roleId, role);
                }
            }
        } catch (Exception e) {
            log.warn("Gateway user info lookup failed: {}", e.getMessage());
            try {
                log.info("Trying direct user-service lookup from: {}/user/me", userServiceUrl);
                String authHeader = buildAuthorizationHeader(authorizationHeader);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authHeader);
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                        userServiceUrl + "/user/me",
                        HttpMethod.GET,
                        entity,
                        Map.class
                );
                
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    Long id = null;
                    String username = null;
                    
                    if (body.get("id") != null) {
                        id = ((Number) body.get("id")).longValue();
                    }
                    if (body.get("username") != null) {
                        username = body.get("username").toString();
                    }
                    
                    if (id != null && username != null) {
                        Integer roleId = null;
                        String role = null;
                        
                        if (body.get("roleId") != null) {
                            roleId = ((Number) body.get("roleId")).intValue();
                        }
                        if (body.get("role") != null) {
                            role = body.get("role").toString();
                        }
                        
                        return new UserInfo(id, username, roleId, role);
                    }
                }
            } catch (Exception ex) {
                log.warn("Direct user-service lookup also failed: {}", ex.getMessage());
            }
        }
        
        return null;
    }

    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO.QuestionDTOBuilder builder = QuestionDTO.builder()
                .id(question.getId())
                .type(question.getType())
                .content(question.getContent())
                .points(question.getPoints())
                .title(question.getTitle())
                .prompt(question.getPrompt())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .transcript(question.getTranscript())
                .rubric(question.getRubric())
                .maxLength(question.getMaxLength())
                .orderIndex(question.getOrderIndex())
                .createdBy(question.getCreatedBy())
                .createdByName(question.getCreatedByName())
                .isShared(question.getIsShared() != null ? question.getIsShared() : false)
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt());

        if (question.getAnswersJson() != null) {
            try {
                List<AnswerDTO> answers = objectMapper.readValue(question.getAnswersJson(), new TypeReference<List<AnswerDTO>>() {});
                builder.answers(answers);
            } catch (Exception e) {
                log.warn("Failed to parse answersJson for question {}", question.getId(), e);
            }
        }

        if (question.getMatchingPairsJson() != null) {
            try {
                List<MatchingPairDTO> matchingPairs = objectMapper.readValue(question.getMatchingPairsJson(), new TypeReference<List<MatchingPairDTO>>() {});
                builder.matchingPairs(matchingPairs);
            } catch (Exception e) {
                log.warn("Failed to parse matchingPairsJson for question {}", question.getId(), e);
            }
        }

        if (question.getBlanksJson() != null) {
            try {
                List<BlankDTO> blanks = objectMapper.readValue(question.getBlanksJson(), new TypeReference<List<BlankDTO>>() {});
                builder.blanks(blanks);
            } catch (Exception e) {
                log.warn("Failed to parse blanksJson for question {}", question.getId(), e);
            }
        }

        return builder.build();
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
}
