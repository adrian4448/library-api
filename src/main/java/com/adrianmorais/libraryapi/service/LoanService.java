package com.adrianmorais.libraryapi.service;

import com.adrianmorais.libraryapi.dto.LoanFilterDto;
import com.adrianmorais.libraryapi.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);
    Optional<Loan> getById(Long id);
    Optional<Loan> update(Loan loan);
    Page<Loan> find(LoanFilterDto dto, Pageable pageable);
}
