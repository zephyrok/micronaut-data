package io.micronaut.data.jdbc.mariadb

import io.micronaut.data.jdbc.mysql.MySqlBookRepository
import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class MariaMultitenancySpec extends AbstractMultitenancySpec {

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MySqlBookRepository.name]
    }

    @Override
    Map<String, String> getDataSourceProperties() {
        return [
                'db-type'        : 'mariadb',
                'schema-generate': 'CREATE_DROP',
                'dialect'        : 'MYSQL'
        ]
    }
}
