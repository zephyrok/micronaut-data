package io.micronaut.data.jdbc.h2


import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class H2MultitenancySpec extends AbstractMultitenancySpec implements H2TestPropertyProvider {

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: H2BookRepository.name]
    }

}
