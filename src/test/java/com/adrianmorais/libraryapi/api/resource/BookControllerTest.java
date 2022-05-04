package com.adrianmorais.libraryapi.api.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adrianmorais.libraryapi.dto.BookDto;
import com.adrianmorais.libraryapi.entity.Book;
import com.adrianmorais.libraryapi.exception.BusinessException;
import com.adrianmorais.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;


@ExtendWith(SpringExtension.class) // mini contexto com classes que pedir para rodar os testes
@ActiveProfiles("test") // configurar ambiente de tesste
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc // configura objeto para realizar as requisicoes
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc; // simula requisicoes para api

    @MockBean
    BookService service;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {
        BookDto dto = createBookDto();
        Book savedBook = Book.builder().id(10L).author("Artur").title("As aventuras").isbn("001").build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        // mock de uma requisição
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("id").value(10L))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar um erro de validação")
    public void createInvalidBookTest() throws Exception {
        String json = new ObjectMapper().writeValueAsString(new BookDto());

        // mock de uma requisição
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já existente")
    public void createBookfffWithDuplicatedIsbn() throws Exception {
        BookDto dto = createBookDto();
        String mensagemErro = "Isbn já cadastrado";
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));

        // Mock de uma requisição
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetails() throws Exception {
        Long id = 1l;
        Book book = Book.builder()
                    .id(1L)
                    .author("Artur")
                    .title("As aventuras")
                    .isbn("001")
                    .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("author").value("Artur"))
                .andExpect(jsonPath("title").value("As aventuras"))
                .andExpect(jsonPath("isbn").value("001"));
    }

    @Test
    @DisplayName("Deve retornar resourceNotFound quando um livro procurado não existir")
    public void bookNotFoundTest() throws Exception {
        Long id = 1l;
        Book book = Book.builder()
                .id(1L)
                .author("Artur")
                .title("As aventuras")
                .isbn("001")
                .build();

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro pelo ID")
    public void deleteBookTest() throws Exception {
        Long id = 1L;

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.of(Book.builder().id(1l).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar resourceNotFound quando não encontrar um livro para deletar")
    public void notFoundBookToDelete() throws Exception {
        Long id = 1l;

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createBookDto());

        Book updatingBook = Book.builder().id(1l).title("some title")
                .author("some author").isbn("01012").build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updatingBook));

        BDDMockito.given(service.update(updatingBook))
                .willReturn(Book.builder().id(1l).author("Artur").title("As aventuras").isbn("001").build());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1l))
                .andExpect(jsonPath("author").value(createBookDto().getAuthor()))
                .andExpect(jsonPath("title").value(createBookDto().getTitle()))
                .andExpect(jsonPath("isbn").value("001"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar atualizar um livro")
    public void updateBookTestError() throws Exception {
        String json = new ObjectMapper().writeValueAsString(createBookDto());

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc
                .perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {
        Long id = 1l;
        Book book = Book.builder()
                .id(id)
                .title(createBookDto().getTitle())
                .author(createBookDto().getIsbn())
                .isbn(createBookDto().getIsbn())
                .build();

        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private BookDto createBookDto() {
        return BookDto.builder().author("Artur").title("As aventuras").isbn("001").build();
    }
}
