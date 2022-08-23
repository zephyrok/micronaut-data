package io.micronaut.data.r2dbc.mysql

import io.micronaut.data.r2dbc.AbstractR2dbcMultitenancySpec

class MySqlMultitenancySpec extends AbstractR2dbcMultitenancySpec {

    @Override
    boolean supportsSchemaMultitenancy() {
        // Requires additional role: GRANT ALL PRIVILEGES ON *.* TO 'test'@'%' WITH GRANT OPTION;
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MySqlBookRepository.name]
    }

    @Override
    Map<String, String> getDataSourceProperties() {
        return [
                'db-type'        : 'mysql',
                'schema-generate': 'CREATE_DROP',
                'dialect'        : 'MYSQL'
        ]
    }
}
