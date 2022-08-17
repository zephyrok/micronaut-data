package io.micronaut.data.mongodb.operations;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.model.PersistentEntity;
import jakarta.inject.Singleton;

@Singleton
@Requires(missingBeans = MongoCollectionNameProvider.class)
public class DefaultMongoCollectionNameProvider implements MongoCollectionNameProvider {

    @Override
    public String provide(PersistentEntity persistentEntity) {
        return persistentEntity.getPersistedName();
    }

}
