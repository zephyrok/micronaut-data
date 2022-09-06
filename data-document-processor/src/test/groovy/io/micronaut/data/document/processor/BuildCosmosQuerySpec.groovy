package io.micronaut.data.document.processor

class BuildCosmosQuerySpec extends AbstractDataSpec {

    void "test cosmos repo"() {
        given:
        def repository = buildRepository('test.CosmosBookRepository', """
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.document.tck.entities.Book;

@CosmosRepository
interface CosmosBookRepository extends GenericRepository<Book, String> {

    boolean existsById(String id);

}
"""
        )

        when:
        String q = TestUtils.getQuery(repository.getRequiredMethod("existsById", String))
        then:
        q == "SELECT true FROM book book_ WHERE (book_.id = @p1)"
    }
}
