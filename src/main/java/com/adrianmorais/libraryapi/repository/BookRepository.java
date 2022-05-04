package com.adrianmorais.libraryapi.repository;

import com.adrianmorais.libraryapi.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
    Optional<Book> findBookByIsbn(String isbn);
}
