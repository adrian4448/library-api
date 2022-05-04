package com.adrianmorais.libraryapi.service;

import com.adrianmorais.libraryapi.dto.LoanFilterDto;
import com.adrianmorais.libraryapi.entity.Book;
import com.adrianmorais.libraryapi.entity.Loan;
import com.adrianmorais.libraryapi.exception.BusinessException;
import com.adrianmorais.libraryapi.repository.LoanRepository;
import com.adrianmorais.libraryapi.service.imp.LoanServiceImp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    @MockBean
    LoanRepository repository;

    @MockBean
    LoanService service;

    @BeforeEach
    public void setUp() {
        this.service = new LoanServiceImp(repository);

    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoanTest() {
        Loan loan = createValidLoan();

        Loan savedLoan = createValidLoan();
        savedLoan.setId(1l);

        Mockito.when(repository.existsByBookAndNotReturned(Mockito.any(Book.class)))
                .thenReturn(false);
        Mockito.when(repository.save(loan)).thenReturn(savedLoan);

        savedLoan = service.save(loan);

        Assertions.assertThat(savedLoan.getId()).isNotNull();
        Assertions.assertThat(savedLoan.getBook()).isEqualTo(createValidBook());
        Assertions.assertThat(savedLoan.getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(savedLoan.getLoanDate()).isEqualTo(loan.getLoanDate());
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar salvar um livro já emprestado")
    public void saveFailLoanTest() {
        Loan loan = createValidLoan();

        Mockito.when(repository.existsByBookAndNotReturned(Mockito.any(Book.class)))
                .thenReturn(true);

        Throwable expection = Assertions.catchThrowable(() -> service.save(loan));

        Assertions.assertThat(expection)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository, Mockito.times(0)).save(loan);
    }

    @Test
    @DisplayName("Deve retornar um emprestimo pelo ID")
    public void returnLoanByIdTest() {
        Long id = 1l;
        Loan loan = createValidLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id))
                .thenReturn(Optional.of(loan));

        Optional<Loan> returnedLoan = service.getById(id);

        Assertions.assertThat(returnedLoan.isPresent()).isTrue();
        Assertions.assertThat(returnedLoan.get().getId()).isEqualTo(loan.getId());
        Assertions.assertThat(returnedLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(returnedLoan.get().getReturned()).isEqualTo(loan.getReturned());
        Assertions.assertThat(returnedLoan.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(returnedLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo")
    public void updateLoanTest() {
        Long id = 1l;
        Loan loan = createValidLoan();
        loan.setId(id);
        loan.setReturned(true);

        Mockito.when(repository.save(loan))
                .thenReturn(loan);

        Optional<Loan> updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.get().getReturned()).isEqualTo(true);

        Mockito.verify(repository, Mockito.times(1)).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar os emprestimos pelas propiedades")
    public void getLoansTest() {
        LoanFilterDto dto = LoanFilterDto.builder().isbn("123").customer("Fulano").build();
        Loan loan = createValidLoan();

        List<Loan> list = Arrays.asList(loan);
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Loan> page = new PageImpl<Loan>(Arrays.asList(loan), pageRequest, 1);

        Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.any(String.class),
                        Mockito.any(String.class),
                        Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> result = service.find(dto, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(list);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    private Loan createValidLoan() {
        return Loan.builder().book(createValidBook()).customer("Fulano").loanDate(LocalDate.now()).build();
    }

    private Book createValidBook() {
        return Book.builder().id(1l).isbn("123").author("Fulano").title("As aventuras").build();
    }

}
