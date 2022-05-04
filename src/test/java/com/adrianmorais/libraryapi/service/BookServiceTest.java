package com.adrianmorais.libraryapi.service;

import com.adrianmorais.libraryapi.entity.Book;
import com.adrianmorais.libraryapi.exception.BusinessException;
import com.adrianmorais.libraryapi.repository.BookRepository;
import com.adrianmorais.libraryapi.service.imp.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service= new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        Book book = createValidBook();

        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book))
                .thenReturn(Book.builder().id(1l).isbn("123").author("Fulano").title("As aventuras").build());

        Book savedBook = service.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
    }

    @Test
    @DisplayName("Deve lançar uma exceção quando já houver livro com esse ISBN")
    public void shouldNotSaveBookWithDuplicatedISBN() {
        Book book = createValidBook();

        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));
        Assertions.assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Isbn já cadastrado");

        Mockito.verify(repository, Mockito.times(0)).save(book);
    }

    @Test
    @DisplayName("Deve buscar um livro por id")
    public void getBookById() {
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> foundBook = service.getById(id);

        Assertions.assertThat(foundBook).isPresent();
        Assertions.assertThat(foundBook.get().getId()).isEqualTo(id);
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe")
    public void bookNotFoundByIdTest() {
        Mockito.when(service.getById(Mockito.anyLong())).thenReturn(Optional.empty());

        Optional<Book> book = service.getById(1l);

        Assertions.assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        Book book = createValidBook();
        book.setId(1l);

        Mockito.when(repository.save(book)).thenReturn(book);

        book = service.update(book);

        Assertions.assertThat(book.getIsbn()).isEqualTo(createValidBook().getIsbn());
        Assertions.assertThat(book.getTitle()).isEqualTo(createValidBook().getTitle());
        Assertions.assertThat(book.getAuthor()).isEqualTo(createValidBook().getAuthor());
    }

    @Test
    @DisplayName("Deve retornar IllegalArgumentException quando o livro não existir ou não possuir id")
    public void updateNotFoundBookTest() {
        Throwable exception = Assertions.catchThrowable(() -> service.update(null));

        Assertions.assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id cant be null.");
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        Book book = createValidBook();
        book.setId(1l);

        service.delete(book);

        Mockito.verify(repository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve lançar uma exceção ao tentar deletar um livro sem id ou inexistente")
    public void dontDeleteBookTest() {
        Throwable exception = Assertions.catchThrowable(() -> service.delete(null));

        Assertions.assertThat(exception).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id cant be null.");
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propiedades")
    public void findBookTest() {
        Book book = createValidBook();

        List<Book> list = Arrays.asList(book);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<Book>(Arrays.asList(book), pageRequest, 1);
        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve buscar um livro pelo ISBN")
    public void findBookByIsbnTest() {
        String isbn = "123";
        Book book = createValidBook();
        book.setId(1l);

        Mockito.when(service.getBookByIsbn(isbn)).thenReturn(Optional.of(book));
        Optional<Book> foundBook = service.getBookByIsbn(isbn);

        Assertions.assertThat(foundBook.isPresent()).isNotNull();
        Assertions.assertThat(foundBook.get().getId()).isEqualTo(1l);
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());

        Mockito.verify(repository, Mockito.times(1)).findBookByIsbn(isbn);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }
}
