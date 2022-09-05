package io.micronaut.data.azure.repositories;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.azure.entities.CosmosBook;
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.document.tck.entities.Book;
import io.micronaut.data.document.tck.repositories.AuthorRepository;
import io.micronaut.data.document.tck.repositories.BookRepository;
import io.micronaut.data.repository.PageableRepository;

@CosmosRepository
public abstract class CosmosBookRepository implements PageableRepository<CosmosBook, String> {

    @Nullable
    public abstract CosmosBook queryById(String id);
}
