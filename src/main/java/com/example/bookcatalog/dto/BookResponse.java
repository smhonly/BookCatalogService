package com.example.bookcatalog.dto;

import com.example.bookcatalog.model.BookDO;

import java.time.Instant;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        Long version,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) {
    public static BookResponse from(BookDO bookDO) {
        return new BookResponse(
                bookDO.getId(),
                bookDO.getTitle(),
                bookDO.getAuthor(),
                bookDO.getIsbn(),
                bookDO.getPublishedYear(),
                bookDO.getVersion(),
                bookDO.getCreatedAt(),
                bookDO.getCreatedBy(),
                bookDO.getUpdatedAt(),
                bookDO.getUpdatedBy()
        );
    }
}
