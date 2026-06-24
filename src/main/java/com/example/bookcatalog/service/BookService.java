package com.example.bookcatalog.service;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.exception.DuplicateResourceException;
import com.example.bookcatalog.exception.ResourceNotFoundException;
import com.example.bookcatalog.model.BookDO;
import com.example.bookcatalog.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<BookDO> list(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public BookDO get(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
    }

    @Transactional
    public BookDO create(BookRequest request) {
        assertIsbnUnique(request.isbn(), null);
        return bookRepository.save(toEntity(request));
    }

    @Transactional
    public BookDO update(Long id, BookRequest request) {
        BookDO bookDO = get(id);
        assertIsbnUnique(request.isbn(), id);
        bookDO.setTitle(request.title());
        bookDO.setAuthor(request.author());
        bookDO.setIsbn(request.isbn());
        bookDO.setPublishedYear(request.publishedYear());
        return bookRepository.save(bookDO);
    }

    @Transactional
    public void delete(Long id) {
        BookDO bookDO = get(id);
        bookDO.setDeleted(true);
    }

    /**
     * Reject if {@code isbn} (when non-null) is already used by another book.
     * Pass {@code excludeId} for the in-flight row during updates, or {@code null}
     * during creation.
     */
    private void assertIsbnUnique(String isbn, Long excludeId) {
        if (isbn == null) {
            return;
        }
        bookRepository.findByIsbn(isbn).ifPresent(existing -> {
            if (!existing.getId().equals(excludeId)) {
                throw new DuplicateResourceException(
                        "Book with ISBN %s already exists".formatted(isbn));
            }
        });
    }

    private static BookDO toEntity(BookRequest request) {
        return BookDO.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publishedYear(request.publishedYear())
                .build();
    }
}
