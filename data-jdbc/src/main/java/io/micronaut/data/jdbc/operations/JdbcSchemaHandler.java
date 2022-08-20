package io.micronaut.data.jdbc.operations;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.data.model.query.builder.sql.Dialect;

import java.sql.Connection;

@Experimental
public interface JdbcSchemaHandler {

    void createSchema(Connection connection, Dialect dialect, String name);

    /**
     * Uses the given schema. Defaults to "SET SCHEMA NAME".
     */
    void useSchema(Connection connection, Dialect dialect, String name);

}
