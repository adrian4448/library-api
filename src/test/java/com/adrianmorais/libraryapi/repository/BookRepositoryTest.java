package com.adrianmorais.libraryapi.repository;

import com.adrianmorais.libraryapi.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenIsbnExists() {
        String isbn = "123";
        entityManager.persist(createValidBook());

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    public void returnFalseWhenIsbnDontExist() {
        String isbn = "123";

        boolean exists = repository.existsByIsbn(isbn);

        Assertions.assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve buscar um livro pelo id")
    public void findByIdTest() {
        Book book = createValidBook();
        entityManager.persist(book);

        Optional<Book> bookFound = repository.findById(book.getId());

        Assertions.assertThat(bookFound.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar um livro na base de dados")
    public void saveBookTest() {
        Book book = createValidBook();

        Book savedBook = repository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        Book book = createValidBook();
        entityManager.persist(book);
        Book persistedBook = entityManager.find(Book.class, book.getId());

        repository.delete(persistedBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());
        Assertions.assertThat(deletedBook).isNull();
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }

}
