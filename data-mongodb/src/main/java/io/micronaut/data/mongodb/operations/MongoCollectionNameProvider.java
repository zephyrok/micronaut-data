package io.micronaut.data.mongodb.operations;

import io.micronaut.data.model.PersistentEntity;

public interface MongoCollectionNameProvider {

    String provide(PersistentEntity persistentEntity);

}
