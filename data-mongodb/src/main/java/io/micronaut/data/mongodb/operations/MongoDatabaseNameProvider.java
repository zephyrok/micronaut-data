package io.micronaut.data.mongodb.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.PersistentEntity;

public interface MongoDatabaseNameProvider {

    @NonNull
    String provide(@NonNull PersistentEntity persistentEntity, @Nullable Class<?> repositoryClass);

    @NonNull
    String provide(@NonNull Class<?> type);

    default String provide(@NonNull PersistentEntity persistentEntity) {
        return provide(persistentEntity, null);
    }

}
