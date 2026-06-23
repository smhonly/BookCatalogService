package com.example.bookcatalog.integration;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.model.BookDO;
import com.example.bookcatalog.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test. Boots the Spring context (real H2, real JPA,
 * real JSON serialization, real exception handler) and drives the public API
 * over HTTP. Complements the slice tests in {@code BookDOControllerTest}
 * (which mock the service) and the unit tests in {@code BookDOServiceTest}
 * (which mock the repository).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private BookRepository bookRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDb() {
        bookRepository.deleteAll();
    }

    @Test
    void fullCrudLifecycle() {
        // Create
        BookRequest create = new BookRequest("Dune", "Frank Herbert", "9780441172719", 1965);
        ResponseEntity<Map> created = rest.postForEntity("/api/v1/books", create, Map.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = ((Number) created.getBody().get("id")).longValue();
        assertThat(created.getHeaders().getLocation()).isEqualTo(java.net.URI.create("/api/v1/books/" + id));
        assertThat(created.getBody()).containsEntry("title", "Dune");
        assertThat(created.getBody()).containsEntry("isbn", "9780441172719");
        assertThat(created.getBody()).containsKey("createdAt"); // @PrePersist fired

        // Read one
        ResponseEntity<Map> fetched = rest.getForEntity("/api/v1/books/" + id, Map.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).containsEntry("id", id.intValue());

        // List
        ResponseEntity<Map> list = rest.getForEntity("/api/v1/books?page=0&size=10", Map.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) list.getBody().get("content")).hasSize(1);

        // Update — same ISBN should not be a conflict (covers assertIsbnUnique excludeId branch)
        BookRequest update = new BookRequest("Dune (1965)", "Frank Herbert", "9780441172719", 1965);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> updated = rest.exchange(
                "/api/v1/books/" + id, HttpMethod.PUT,
                new HttpEntity<>(update, headers), Map.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).containsEntry("title", "Dune (1965)");

        // Delete
        ResponseEntity<Void> deleted = rest.exchange(
                "/api/v1/books/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(bookRepository.count()).isZero();
    }

    @Test
    void getMissingReturns404() {
        ResponseEntity<Map> resp = rest.getForEntity("/api/v1/books/999", Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).containsEntry("status", 404);
        assertThat(resp.getBody()).containsEntry("error", "Not Found");
        assertThat((String) resp.getBody().get("message")).contains("999");
    }

    @Test
    void createDuplicateIsbnReturns409() {
        rest.postForEntity("/api/v1/books",
                new BookRequest("A", "X", "9780441172719", 2000), Map.class);

        ResponseEntity<Map> resp = rest.postForEntity("/api/v1/books",
                new BookRequest("B", "Y", "9780441172719", 2001), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody()).containsEntry("status", 409);
        assertThat((String) resp.getBody().get("message")).contains("9780441172719");
    }

    @Test
    void createBlankTitleReturns400() {
        BookRequest bad = new BookRequest("", "Some Author", "1234567890123", 2020);
        ResponseEntity<Map> resp = rest.postForEntity("/api/v1/books", bad, Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).containsEntry("status", 400);
        assertThat((String) resp.getBody().get("message")).contains("title");
    }

    @Test
    void listReturnsEmptyPageWhenDbEmpty() {
        ResponseEntity<Map> resp = rest.getForEntity("/api/v1/books", Map.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) resp.getBody().get("content")).isEmpty();
        assertThat(resp.getBody()).containsEntry("totalElements", 0);
    }
}
