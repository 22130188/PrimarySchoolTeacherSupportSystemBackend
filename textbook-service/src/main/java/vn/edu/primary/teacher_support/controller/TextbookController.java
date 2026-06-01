package vn.edu.primary.teacher_support.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.primary.teacher_support.dto.BookDto;
import vn.edu.primary.teacher_support.exception.ResourceNotFoundException;
import vn.edu.primary.teacher_support.service.TextbookService;

import java.util.List;

@RestController
@RequestMapping("/api/textbooks")
public class TextbookController {

    @Autowired
    private TextbookService textbookService;

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllTextbooks(
            @RequestParam(value = "grade", required = false) Integer grade,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "bookType", required = false) String bookType,
            @RequestParam(value = "search", required = false) String search) {
        
        List<BookDto> books = textbookService.getAllTextbooks(grade, subject, bookType, search);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{slugId}")
    public ResponseEntity<BookDto> getTextbookBySlug(@PathVariable("slugId") String slugId) {
        return textbookService.getTextbookBySlug(slugId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sách với slug_id: " + slugId));
    }
}
