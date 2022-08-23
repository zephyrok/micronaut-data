package io.micronaut.data.jdbc.mysql

import io.micronaut.data.jdbc.AbstractJdbcMultitenancySpec

class MySqlMultitenancySpec extends AbstractJdbcMultitenancySpec {

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
