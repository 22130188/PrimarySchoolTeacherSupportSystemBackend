package vn.edu.primary.teacher_support.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.primary.teacher_support.dto.BookDto;
import vn.edu.primary.teacher_support.dto.PageDto;
import vn.edu.primary.teacher_support.entity.Book;
import vn.edu.primary.teacher_support.repository.BookRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TextbookService {

    @Autowired
    private BookRepository bookRepository;

    public List<BookDto> getAllTextbooks(Integer grade, String subject, String bookType, String search) {
        String querySearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String querySubject = (subject != null && !subject.trim().isEmpty()) ? subject.trim() : null;
        String queryBookType = (bookType != null && !bookType.trim().isEmpty()) ? bookType.trim() : null;
        return bookRepository.findBooks(grade, querySubject, queryBookType, querySearch)
                .stream()
                .map(this::convertToListDto)
                .collect(Collectors.toList());
    }

    public Optional<BookDto> getTextbookBySlug(String slugId) {
        return bookRepository.findBySlugId(slugId)
                .map(this::convertToDetailDto);
    }

    private BookDto convertToListDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getGrade(),
                book.getSubject(),
                book.getCoverUrl(),
                book.getSlugId(),
                book.getUrl(),
                book.getBookType(),
                null
        );
    }

    private BookDto convertToDetailDto(Book book) {
        List<PageDto> pageDtos = null;
        if (book.getPages() != null) {
            pageDtos = book.getPages().stream()
                    .map(page -> new PageDto(page.getId(), page.getPageNumber(), page.getImageUrl()))
                    .collect(Collectors.toList());
        }

        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getGrade(),
                book.getSubject(),
                book.getCoverUrl(),
                book.getSlugId(),
                book.getUrl(),
                book.getBookType(),
                pageDtos
        );
    }
}
