package io.micronaut.data.document.tck.repositories;

import io.micronaut.data.document.tck.entities.Book;
import io.micronaut.data.document.tck.entities.BookDto;
import io.micronaut.data.repository.GenericRepository;

import java.util.Optional;

public abstract class BookDtoRepository implements GenericRepository<Book, String> {

    public abstract Optional<BookDto> findByTitleAndTotalPages(String title, int totalPages);

    public abstract Optional<BookDto> findById(String id);
}
