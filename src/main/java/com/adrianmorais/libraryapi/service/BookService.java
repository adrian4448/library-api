package com.adrianmorais.libraryapi.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.adrianmorais.libraryapi.model.entity.Book;

public interface BookService {

	public Book save(Book book);

	public Optional<Book> getById(Long id);

	public void delete(Book book);

	public Book update(Book book);

	public Page<Book> find(Book filter, Pageable pageRequest);

	public Optional<Book> getBookByIsbn(String isbn);

}
