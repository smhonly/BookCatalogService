package com.example.bookcatalog.repository;

import com.example.bookcatalog.model.BookDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<BookDO, Long> {

    Optional<BookDO> findByIsbn(String isbn);
}
