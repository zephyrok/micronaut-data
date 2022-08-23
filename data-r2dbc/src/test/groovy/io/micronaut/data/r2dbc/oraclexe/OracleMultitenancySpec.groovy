package io.micronaut.data.r2dbc.oraclexe

import io.micronaut.data.r2dbc.AbstractR2dbcMultitenancySpec

class OracleMultitenancySpec extends AbstractR2dbcMultitenancySpec {

    @Override
    boolean supportsSchemaMultitenancy() {
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: OracleXEBookRepository.name]
    }
}
