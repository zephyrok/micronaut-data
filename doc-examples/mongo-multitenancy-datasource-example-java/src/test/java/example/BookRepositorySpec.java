package example;

import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class BookRepositorySpec {

    @Inject
    BookRepository bookRepository;

    @Inject
    BeanContext beanContext;

    @AfterEach
    public void cleanup() {
        bookRepository.deleteAll();
    }

    @Test
    void testCrud() {
        assertNotNull(bookRepository);

        // Create: Save a new book
        // tag::save[]
        Book book = new Book("The Stand", 1000);
        bookRepository.save(book);
        // end::save[]
        ObjectId id = book.getId();
        assertNotNull(id);

        // Read: Read a book from the database
        // tag::read[]
        book = bookRepository.findById(id).orElse(null);
        // end::read[]
        assertNotNull(book);
        assertEquals("The Stand", book.getTitle());

        // Check the count
        assertEquals(1, bookRepository.count());
        assertTrue(bookRepository.findAll().iterator().hasNext());

        // Delete: Delete the book
        // tag::delete[]
        bookRepository.deleteById(id);
        // end::delete[]
        assertEquals(0, bookRepository.count());
    }
}
