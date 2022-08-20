package io.micronaut.data.jdbc.sqlserver


import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class SqlServerMultitenancySpec extends AbstractMultitenancySpec implements MSSQLTestPropertyProvider {

    @Override
    boolean supportsSchemaMultitenancy() {
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MSBookRepository.name]
    }
}
