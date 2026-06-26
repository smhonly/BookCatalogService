package com.example.bookcatalog.controller;

import com.example.bookcatalog.dto.BookRequest;
import com.example.bookcatalog.model.BookDO;
import com.example.bookcatalog.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import com.example.bookcatalog.exception.ResourceNotFoundException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for the books controller. No security layer is wired (this is a
 * pure-CRUD service), so {@code addFilters = false} keeps the slice minimal.
 */
@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void listReturnsPageOfBooks() throws Exception {
        BookDO bookDO = BookDO.builder().id(1L).title("Dune").author("Frank Herbert").build();
        Page<BookDO> page = new PageImpl<>(List.of(bookDO), PageRequest.of(0, 20), 1);
        when(bookService.list(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Dune"))
                .andExpect(jsonPath("$.content[0].author").value("Frank Herbert"));
    }

    @Test
    void getReturnsBook() throws Exception {
        BookDO bookDO = BookDO.builder().id(1L).title("Dune").author("Frank Herbert").build();
        when(bookService.get(1L)).thenReturn(bookDO);

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Dune"));
    }

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        BookRequest request = new BookRequest("Dune", "Frank Herbert", "9780441172719", 1965, null);
        BookDO saved = BookDO.builder().id(42L).title("Dune").author("Frank Herbert").build();
        when(bookService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/books/42"))
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test
    void updateReturnsUpdatedBook() throws Exception {
        BookRequest request = new BookRequest("Dune", "Frank Herbert", "9780441172719", 1965, 1L);
        BookDO updated = BookDO.builder().id(1L).title("Dune").author("Frank Herbert").build();
        when(bookService.update(any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new ResourceNotFoundException("Book", 99L))
                .when(bookService).delete(99L);

        mockMvc.perform(delete("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
