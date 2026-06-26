package com.example.bookcatalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 120) String author,
        @Size(max = 20) String isbn,
        @Min(0) Integer publishedYear,
        Long version
) {
}
