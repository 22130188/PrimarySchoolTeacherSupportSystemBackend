package vn.edu.primary.teacher_support.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.entity.*;
import vn.edu.primary.teacher_support.repository.*;
import vn.edu.primary.teacher_support.service.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GuideController {
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/api/guides")
    @Transactional
    public List<GuideResponse> published() { return guideRepository.findByPublishedTrueOrderBySortOrderAsc().stream().map(this::response).toList(); }

    @GetMapping("/api/admin/guides")
    @Transactional
    public List<GuideResponse> all(@RequestHeader(value="Authorization", required=false) String auth) { admin(auth); return guideRepository.findAllByOrderBySortOrderAsc().stream().map(this::response).toList(); }

    @PostMapping("/api/admin/guides")
    @Transactional
    public GuideResponse create(@RequestHeader(value="Authorization", required=false) String auth, @Valid @RequestBody GuideRequest request) {
        admin(auth); if (guideRepository.findBySlug(request.slug()).isPresent()) throw new IllegalArgumentException("Đường dẫn hướng dẫn đã tồn tại");
        Guide guide = apply(new Guide(), request); guide.setSortOrder(guideRepository.findAllByOrderBySortOrderAsc().size()); return response(guideRepository.save(guide));
    }

    @PutMapping("/api/admin/guides/{id}")
    @Transactional
    public GuideResponse update(@RequestHeader(value="Authorization", required=false) String auth, @PathVariable Long id, @Valid @RequestBody GuideRequest request) {
        admin(auth); Guide guide = guideRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hướng dẫn"));
        guideRepository.findBySlug(request.slug()).filter(found -> !found.getId().equals(id)).ifPresent(found -> { throw new IllegalArgumentException("Đường dẫn hướng dẫn đã tồn tại"); });
        return response(guideRepository.save(apply(guide, request)));
    }

    @DeleteMapping("/api/admin/guides/{id}")
    public Map<String, Boolean> delete(@RequestHeader(value="Authorization", required=false) String auth, @PathVariable Long id) { admin(auth); guideRepository.deleteById(id); return Map.of("deleted", true); }

    @PatchMapping("/api/admin/guides/reorder")
    @Transactional
    public ResponseEntity<Void> reorder(@RequestHeader(value="Authorization", required=false) String auth, @RequestBody ReorderRequest request) {
        admin(auth); for (int i=0; i<request.ids().size(); i++) { int order=i; guideRepository.findById(request.ids().get(i)).ifPresent(g -> { g.setSortOrder(order); guideRepository.save(g); }); } return ResponseEntity.noContent().build();
    }

    private Guide apply(Guide guide, GuideRequest request) {
        guide.setSlug(request.slug().trim()); guide.setTitle(request.title().trim()); guide.setDescription(request.description()); guide.setNote(request.note()); guide.setPublished(request.published());
        guide.replaceSteps(request.steps().stream().map(item -> { GuideStep step = new GuideStep(); step.setTitle(item.title().trim()); step.setContent(item.content().trim()); step.setImageUrl(item.imageUrl()); step.setImageAlt(item.imageAlt()); step.setVideoUrl(item.videoUrl()); step.setSortOrder(item.sortOrder()); return step; }).toList());
        return guide;
    }
    private GuideResponse response(Guide g) { return new GuideResponse(g.getId(),g.getSlug(),g.getTitle(),g.getDescription(),g.getNote(),g.isPublished(),g.getSortOrder(),g.getCreatedAt(),g.getUpdatedAt(),g.getSteps().stream().map(s -> new StepResponse(s.getId(),s.getTitle(),s.getContent(),s.getImageUrl(),s.getImageAlt(),s.getVideoUrl(),s.getSortOrder())).toList()); }
    private User admin(String authorization) { if (authorization==null || !authorization.startsWith("Bearer ")) throw new RuntimeException("Thiếu token xác thực"); String token=authorization.substring(7); if (!jwtService.isValid(token)) throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn"); User user=userRepository.findByUsername(jwtService.extractUsername(token)).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")); if (user.getRole()!=Role.RoleName.ADMIN && user.getRoles().stream().noneMatch(r -> r.getName()==Role.RoleName.ADMIN)) throw new RuntimeException("Bạn không có quyền quản lý hướng dẫn"); return user; }

    public record GuideRequest(@NotBlank @Size(max=160) String slug, @NotBlank @Size(max=220) String title, @Size(max=600) String description, String note, boolean published, @Valid @Size(min=1) List<StepRequest> steps) {}
    public record StepRequest(@NotBlank @Size(max=220) String title, @NotBlank String content, @Size(max=1000) String imageUrl, @Size(max=300) String imageAlt, @Size(max=1000) String videoUrl, int sortOrder) {}
    public record ReorderRequest(List<Long> ids) {}
    public record GuideResponse(Long id,String slug,String title,String description,String note,boolean published,int sortOrder,LocalDateTime createdAt,LocalDateTime updatedAt,List<StepResponse> steps) {}
    public record StepResponse(Long id,String title,String content,String imageUrl,String imageAlt,String videoUrl,int sortOrder) {}
}
