package com.example.bookcatalog.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Light unit test for the {@code @PrePersist} callback. The integration
 * test in {@code BookIntegrationTest} already proves the callback fires
 * through real JPA, but pinning it down at the unit level documents the
 * behaviour and keeps coverage above the JaCoCo gate.
 */
class BookDOTest {

    @Test
    void onPersistSetsCreatedAtWhenNull() {
        BookDO bookDO = BookDO.builder()
                .title("Dune")
                .author("Frank Herbert")
                .build();
        assertThat(bookDO.getCreatedAt()).isNull();

        bookDO.onPersist();

        assertThat(bookDO.getCreatedAt()).isNotNull();
        assertThat(bookDO.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void onPersistLeavesExistingCreatedAtAlone() {
        Instant preset = Instant.parse("2020-01-01T00:00:00Z");
        BookDO bookDO = BookDO.builder()
                .title("Dune")
                .author("Frank Herbert")
                .createdAt(preset)
                .build();

        bookDO.onPersist();

        assertThat(bookDO.getCreatedAt()).isEqualTo(preset);
    }
}
