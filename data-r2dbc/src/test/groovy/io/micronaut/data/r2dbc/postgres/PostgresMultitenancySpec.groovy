package io.micronaut.data.r2dbc.postgres

import io.micronaut.data.r2dbc.AbstractR2dbcMultitenancySpec

class PostgresMultitenancySpec extends AbstractR2dbcMultitenancySpec {

    @Override
    Map<String, String> getExtraProperties() {
        return [
                'bookRepositoryClass': PostgresBookRepository.name
        ]
    }

    @Override
    Map<String, String> getDataSourceProperties() {
        return [
                'db-type'        : 'postgresql',
                'schema-generate': 'CREATE_DROP',
                'dialect'        : 'POSTGRES'
        ]
    }
}
