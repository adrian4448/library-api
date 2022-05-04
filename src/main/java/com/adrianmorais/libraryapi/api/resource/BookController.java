package com.adrianmorais.libraryapi.api.resource;

import com.adrianmorais.libraryapi.api.exception.ApiErrors;
import com.adrianmorais.libraryapi.entity.Book;
import com.adrianmorais.libraryapi.exception.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import com.adrianmorais.libraryapi.dto.BookDto;
import com.adrianmorais.libraryapi.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
@Slf4j
public class BookController {

    private BookService service;
    private ModelMapper modelMapper;

    public BookController(BookService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Cria um livro na base de dados")
    public BookDto create( @RequestBody @Valid BookDto dto) {
        log.info("creating a book for isbn: {}", dto.getIsbn());
        Book entity = modelMapper.map(dto, Book.class);
        entity = service.save(entity);
        return modelMapper.map(entity, BookDto.class);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Busca um livro na base de dados pelo seu ID")
    public BookDto get( @PathVariable Long id) {
        log.info(" obtaining details for book id: {}", id);
        return this.service.getById(id)
                .map( book -> this.modelMapper.map(book, BookDto.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Busca paginada de livros na base de dados")
    public Page<BookDto> find(BookDto dto, Pageable pageRequest) {
        log.info(" obtaning books from params: {}", dto);
        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);
        List<BookDto> list = result.getContent()
                .stream()
                .map( entity -> modelMapper.map(entity, BookDto.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDto>(list, pageRequest, result.getTotalElements());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Deleta um livro na base de dados pelo ID")
    public void delete(@PathVariable Long id) {
        log.info(" deleting  book from id: {}", id);
        service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Atualiza um livro na base de dados pelo seu ID")
    public BookDto update(@RequestBody BookDto dto, @PathVariable Long id) {
        log.info(" updating book: {}", dto);
        return service.getById(id)
                .map(book -> {
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());
                    book = service.update(book);
                    return modelMapper.map(book, BookDto.class);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


    }

}
