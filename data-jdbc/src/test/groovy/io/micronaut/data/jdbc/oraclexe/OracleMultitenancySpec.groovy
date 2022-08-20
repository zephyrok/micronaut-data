package io.micronaut.data.jdbc.oraclexe


import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class OracleMultitenancySpec extends AbstractMultitenancySpec implements OracleTestPropertyProvider {

    @Override
    boolean supportsSchemaMultitenancy() {
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: OracleXEBookRepository.name]
    }
}
