package io.micronaut.data.r2dbc.sqlserver

import io.micronaut.data.r2dbc.AbstractR2dbcMultitenancySpec

class SqlServerMultitenancySpec extends AbstractR2dbcMultitenancySpec {

    @Override
    boolean supportsSchemaMultitenancy() {
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MSBookRepository.name]
    }
}
