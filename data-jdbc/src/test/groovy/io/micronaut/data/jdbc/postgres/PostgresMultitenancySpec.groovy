package io.micronaut.data.jdbc.postgres


import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class PostgresMultitenancySpec extends AbstractMultitenancySpec {

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
