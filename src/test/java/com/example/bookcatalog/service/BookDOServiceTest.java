package com.example.bookcatalog.service;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.exception.DuplicateResourceException;
import com.example.bookcatalog.exception.ResourceNotFoundException;
import com.example.bookcatalog.model.BookDO;
import com.example.bookcatalog.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookDOServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private BookDO sample;

    @BeforeEach
    void setUp() {
        sample = BookDO.builder()
                .id(1L)
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("9780134685991")
                .publishedYear(2018)
                .build();
    }

    @Test
    void createPersistsBookWhenIsbnIsUnique() {
        BookRequest request = new BookRequest("Effective Java", "Joshua Bloch", "9780134685991", 2018);
        when(bookRepository.findByIsbn("9780134685991")).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookDO.class))).thenAnswer(inv -> inv.getArgument(0));

        BookDO created = bookService.create(request);

        ArgumentCaptor<BookDO> captor = ArgumentCaptor.forClass(BookDO.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Effective Java");
        assertThat(captor.getValue().getIsbn()).isEqualTo("9780134685991");
        assertThat(created.getTitle()).isEqualTo("Effective Java");
    }

    @Test
    void createRejectsDuplicateIsbn() {
        BookRequest request = new BookRequest("Effective Java", "Joshua Bloch", "9780134685991", 2018);
        when(bookRepository.findByIsbn("9780134685991")).thenReturn(Optional.of(sample));

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getThrowsWhenMissing() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateUpdatesBookFields() {
        BookRequest request = new BookRequest(
                "Effective Java, 3rd ed.", "Joshua Bloch", "9780134685991", 2018);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(bookRepository.findByIsbn("9780134685991")).thenReturn(Optional.empty());
        when(bookRepository.save(any(BookDO.class))).thenAnswer(inv -> inv.getArgument(0));

        BookDO updated = bookService.update(1L, request);

        ArgumentCaptor<BookDO> captor = ArgumentCaptor.forClass(BookDO.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Effective Java, 3rd ed.");
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(updated.getTitle()).isEqualTo("Effective Java, 3rd ed.");
    }

    @Test
    void updateRejectsDuplicateIsbn() {
        BookDO other = BookDO.builder()
                .id(2L)
                .title("Other Book")
                .author("Someone Else")
                .isbn("9999999999999")
                .build();
        BookRequest request = new BookRequest("Effective Java", "Joshua Bloch", "9999999999999", 2018);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(bookRepository.findByIsbn("9999999999999")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> bookService.update(1L, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(bookRepository, never()).save(any());
    }
}
