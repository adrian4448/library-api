package com.adrianmorais.libraryapi.api.resource;

import com.adrianmorais.libraryapi.dto.BookDto;
import com.adrianmorais.libraryapi.dto.LoanDto;
import com.adrianmorais.libraryapi.dto.LoanFilterDto;
import com.adrianmorais.libraryapi.dto.ReturnedLoanDto;
import com.adrianmorais.libraryapi.entity.Book;
import com.adrianmorais.libraryapi.entity.Loan;
import com.adrianmorais.libraryapi.service.BookService;
import com.adrianmorais.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.Model;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@Api("Loan API")
@Slf4j
public class LoanController {

    private LoanService service;

    private BookService bookService;

    private ModelMapper modelMapper;

    public LoanController(LoanService service, BookService bookService, ModelMapper modelMapper) {
        this.service = service;
        this.bookService = bookService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Realiza um emprestimo")
    public Long create(@RequestBody LoanDto dto) {
        log.info("Creating a loan from: {}", dto);
        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book).loanDate(LocalDate.now()).customer(dto.getCustomer()).build();

        entity = service.save(entity);

        return entity.getId();
    }
    
    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public void returnLoan(@PathVariable Long id, @RequestBody ReturnedLoanDto dto) {
        log.info("update loan returned value from id: {}", id);
        Loan loan = service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.isReturned());
        service.update(loan);
    }

    @GetMapping
    @ApiOperation("Find loans by params")
    public Page<LoanDto> find(LoanFilterDto dto, Pageable pageRequest) {
        Page<Loan> result = service.find(dto, pageRequest);

        List<LoanDto> loans = result.getContent().stream()
                .map(entity -> {
                    Book book = entity.getBook();
                    BookDto bookDTO = modelMapper.map(book, BookDto.class);
                    LoanDto loanDTO = modelMapper.map(entity, LoanDto.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(loans, pageRequest, result.getTotalElements());
    }
}
