package io.micronaut.data.r2dbc.operations;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.r2dbc.spi.Connection;
import org.reactivestreams.Publisher;

@Experimental
public interface R2dbcSchemaHandler {

    Publisher<Void> createSchema(Connection connection, Dialect dialect, String name);

    /**
     * Uses the given schema. Defaults to "SET SCHEMA NAME".
     */
    Publisher<Void> useSchema(Connection connection, Dialect dialect, String name);

}
