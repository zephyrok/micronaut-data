package io.micronaut.data.mongodb.operations;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.conf.MongoDataConfiguration;
import io.micronaut.data.repository.GenericRepository;
import io.micronaut.data.runtime.multitenancy.SchemaTenantResolver;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefaultMongoDatabaseNameProvider implements MongoDatabaseNameProvider {

    private final RuntimeEntityRegistry runtimeEntityRegistry;
    private final Map<Class, String> repoDatabaseConfig;
    @Nullable
    private final String defaultDatabaseName;
    @Nullable
    private final SchemaTenantResolver tenantResolver;

    public DefaultMongoDatabaseNameProvider(BeanContext beanContext,
                                            @Nullable String server,
                                            RuntimeEntityRegistry runtimeEntityRegistry,
                                            @Nullable String defaultDatabaseName,
                                            @Nullable SchemaTenantResolver tenantResolver) {
        this.runtimeEntityRegistry = runtimeEntityRegistry;
        this.defaultDatabaseName = defaultDatabaseName;
        this.tenantResolver = tenantResolver;
        Collection<BeanDefinition<GenericRepository>> beanDefinitions = beanContext
            .getBeanDefinitions(GenericRepository.class, Qualifiers.byStereotype(MongoRepository.class));
        HashMap<Class, String> repoDatabaseConfig = new HashMap<>();
        for (BeanDefinition<GenericRepository> beanDefinition : beanDefinitions) {
            String targetSrv = beanDefinition.stringValue(Repository.class).orElse(null);
            if (targetSrv == null || targetSrv.isEmpty() || targetSrv.equalsIgnoreCase(server)) {
                String database = beanDefinition.stringValue(MongoRepository.class, "databaseName").orElse(null);
                if (StringUtils.isNotEmpty(database)) {
                    repoDatabaseConfig.put(beanDefinition.getBeanType(), database);
                }
            }
        }
        this.repoDatabaseConfig = Collections.unmodifiableMap(repoDatabaseConfig);
    }

    @Override
    public String provide(Class<?> type) {
        return provide(runtimeEntityRegistry.getEntity(type));
    }

    @Override
    public String provide(PersistentEntity persistentEntity, Class<?> repositoryClass) {
        if (tenantResolver != null) {
            String database = tenantResolver.resolveTenantSchemaName();
            if (database != null) {
                return database;
            }
        }
        if (repositoryClass != null) {
            String database = repoDatabaseConfig.get(repositoryClass);
            if (database != null) {
                return database;
            }
        }
        if (defaultDatabaseName == null) {
            throw new DataAccessException(MongoDataConfiguration.DATABASE_CONFIGURATION_ERROR_MESSAGE);
        }
        return defaultDatabaseName;
    }

}
