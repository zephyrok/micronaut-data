package io.micronaut.data.jdbc.sqlserver

import io.micronaut.data.jdbc.AbstractJdbcMultitenancySpec

class SqlServerMultitenancySpec extends AbstractJdbcMultitenancySpec {

    @Override
    boolean supportsSchemaMultitenancy() {
        return false
    }

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MSBookRepository.name]
    }

    @Override
    Map<String, String> getDataSourceProperties() {
        return [
                'db-type'        : 'mssql',
                'schema-generate': 'CREATE_DROP',
                'dialect'        : 'ORACLE'
        ]
    }
}
