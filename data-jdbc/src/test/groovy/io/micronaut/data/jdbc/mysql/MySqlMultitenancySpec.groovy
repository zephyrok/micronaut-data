package io.micronaut.data.jdbc.mysql


import io.micronaut.data.tck.tests.AbstractMultitenancySpec

class MySqlMultitenancySpec extends AbstractMultitenancySpec implements MySQLTestPropertyProvider {

    @Override
    Map<String, String> getExtraProperties() {
        return [bookRepositoryClass: MySqlBookRepository.name]
    }
}
