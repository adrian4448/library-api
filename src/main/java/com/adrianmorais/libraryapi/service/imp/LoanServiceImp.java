package com.adrianmorais.libraryapi.service.imp;

import com.adrianmorais.libraryapi.dto.LoanFilterDto;
import com.adrianmorais.libraryapi.entity.Loan;
import com.adrianmorais.libraryapi.exception.BusinessException;
import com.adrianmorais.libraryapi.repository.LoanRepository;
import com.adrianmorais.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImp implements LoanService {

    private LoanRepository repository;

    public LoanServiceImp(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if(repository.existsByBookAndNotReturned(loan.getBook())) {
            throw new BusinessException("Book already loaned");
        }

        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Loan> update(Loan loan) {
        return Optional.of(repository.save(loan));
    }

    @Override
    public Page<Loan> find(LoanFilterDto dto, Pageable pageable) {
        return repository.findByBookIsbnOrCustomer(dto.getIsbn(), dto.getCustomer(), pageable);
    }

}
