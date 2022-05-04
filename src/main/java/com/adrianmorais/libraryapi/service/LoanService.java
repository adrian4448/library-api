package com.adrianmorais.libraryapi.service;

import java.util.List;
import java.util.Optional;

import com.adrianmorais.libraryapi.api.dto.LoanFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.adrianmorais.libraryapi.model.entity.Book;
import com.adrianmorais.libraryapi.model.entity.Loan;

public interface LoanService {

	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);

	Page<Loan> find(LoanFilterDTO filter, Pageable pageable);

	Page<Loan> getLoansByBook(Book book, Pageable pageable);

	List<Loan> getAllLateLoans();
}
