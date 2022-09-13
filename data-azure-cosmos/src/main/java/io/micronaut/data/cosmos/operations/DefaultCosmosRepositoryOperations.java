/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.cosmos.operations;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.cosmos.annotation.Container;
import io.micronaut.data.cosmos.config.CosmosDatabaseConfiguration;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.exceptions.NonUniqueResultException;
import io.micronaut.data.model.DataType;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import io.micronaut.data.runtime.operations.internal.AbstractRepositoryOperations;
import io.micronaut.data.runtime.operations.internal.sql.DefaultSqlPreparedQuery;
import io.micronaut.data.runtime.operations.internal.sql.DefaultSqlStoredQuery;
import io.micronaut.data.runtime.operations.internal.sql.SqlPreparedQuery;
import io.micronaut.data.runtime.operations.internal.sql.SqlStoredQuery;
import io.micronaut.data.runtime.query.MethodContextAwareStoredQueryDecorator;
import io.micronaut.data.runtime.query.PreparedQueryDecorator;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.jackson.core.tree.JsonNodeTreeCodec;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.SerdeRegistry;
import io.micronaut.serde.Serializer;
import io.micronaut.serde.jackson.JacksonDecoder;
import io.micronaut.serde.support.util.JsonNodeEncoder;
import jakarta.inject.Singleton;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The default Azure Cosmos DB operations implementation.
 */
@Singleton
@Requires(bean = CosmosClient.class)
@Internal
final class DefaultCosmosRepositoryOperations extends AbstractRepositoryOperations implements CosmosRepositoryOperations,
    PreparedQueryDecorator, MethodContextAwareStoredQueryDecorator {

    private static final int NOT_FOUND_STATUS_CODE = 404;
    // This should return exact collection item by the id in given container
    private static final String FIND_ONE_DEFAULT_QUERY = "SELECT * FROM root WHERE root.id = @ROOT_ID";

    private static final Logger QUERY_LOG = DataSettings.QUERY_LOG;

    private final CosmosClient cosmosClient;
    private final SerdeRegistry serdeRegistry;
    private final ObjectMapper objectMapper;
    private final CosmosDatabase database;
    private final Map<PersistentEntity, CosmosContainerProperties> cosmosEntityProperties = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     *
     * @param codecs                     The media type codecs
     * @param dateTimeProvider           The date time provider
     * @param runtimeEntityRegistry      The entity registry
     * @param conversionService          The conversion service
     * @param attributeConverterRegistry The attribute converter registry
     * @param cosmosClient               The Cosmos client
     * @param serdeRegistry              The (de)serialization registry
     * @param objectMapper               The object mapper used for the data (de)serialization
     * @param configuration              The Cosmos database configuration
     */
    protected DefaultCosmosRepositoryOperations(List<MediaTypeCodec> codecs,
                                                DateTimeProvider<Object> dateTimeProvider,
                                                RuntimeEntityRegistry runtimeEntityRegistry,
                                                DataConversionService<?> conversionService,
                                                AttributeConverterRegistry attributeConverterRegistry,
                                                CosmosClient cosmosClient,
                                                SerdeRegistry serdeRegistry,
                                                ObjectMapper objectMapper,
                                                CosmosDatabaseConfiguration configuration) {
        super(codecs, dateTimeProvider, runtimeEntityRegistry, conversionService, attributeConverterRegistry);
        this.cosmosClient = cosmosClient;
        this.serdeRegistry = serdeRegistry;
        this.objectMapper = objectMapper;
        this.database = initDatabase(configuration);
    }

    private CosmosDatabase initDatabase(CosmosDatabaseConfiguration configuration) {
        CosmosDatabaseResponse databaseResponse;
        ThroughputProperties throughputProperties = createThroughputProperties(configuration);
        if (throughputProperties == null) {
            databaseResponse = cosmosClient.createDatabaseIfNotExists(configuration.getDatabaseName());
        } else {
            databaseResponse = cosmosClient.createDatabaseIfNotExists(configuration.getDatabaseName(), throughputProperties);
        }
        CosmosDatabase cosmosDatabase = cosmosClient.getDatabase(databaseResponse.getProperties().getId());
        initContainers(cosmosDatabase);
        return cosmosDatabase;
    }

    private void initContainers(CosmosDatabase cosmosDatabase) {
        // Find entities
        Collection<BeanIntrospection<Object>> introspections = BeanIntrospector.SHARED.findIntrospections(MappedEntity.class);
        PersistentEntity[] entities = introspections.stream()
            // filter out inner / internal / abstract(MappedSuperClass) classes
            .filter(i -> !i.getBeanType().getName().contains("$"))
            .filter(i -> !java.lang.reflect.Modifier.isAbstract(i.getBeanType().getModifiers()))
            .filter(i -> i.hasAnnotation(Container.class))
            .map(e -> runtimeEntityRegistry.getEntity(e.getBeanType())).toArray(PersistentEntity[]::new);
        for (PersistentEntity entity : entities) {
            AnnotationValue<Container> containerData = entity.getAnnotation(Container.class);
            if (containerData != null) {
                String containerName = containerData.stringValue("name").orElse("");
                if (StringUtils.isEmpty(containerName)) {
                    containerName = entity.getPersistedName();
                }
                String partitionKeyPath = findPartitionKey(entity);
                if (StringUtils.isEmpty(partitionKeyPath)) {
                    // If not defined on any field, use from the container (also may be not defined on container level)
                    partitionKeyPath = containerData.stringValue("partitionKeyPath").orElse("");
                }
                int throughputRequestUnits = containerData.intValue("throughputRequestUnits").orElse(0);
                ThroughputProperties throughputProperties = null;
                if (throughputRequestUnits > 0) {
                    boolean throughputAutoScale = containerData.booleanValue("throughputAutoScale").orElse(false);
                    if (throughputAutoScale) {
                        throughputProperties = ThroughputProperties.createAutoscaledThroughput(throughputRequestUnits);
                    } else {
                        throughputProperties = ThroughputProperties.createManualThroughput(throughputRequestUnits);
                    }
                }
                CosmosContainerProperties props = new CosmosContainerProperties(containerName, partitionKeyPath, throughputProperties);
                cosmosEntityProperties.putIfAbsent(entity, props);
                if (containerData.booleanValue("autoCreate").orElse(false)) {
                    createContainer(cosmosDatabase, entity);
                }
            }
        }
    }

    private String findPartitionKey(PersistentEntity entity) {
        String partitionKeyPath = "";
        List<PersistentProperty> properties = new ArrayList<>(entity.getPersistentProperties());
        PersistentProperty identity = entity.getIdentity();
        if (identity != null) {
            properties.add(0, identity);
        }
        // Find partition key path
        for (PersistentProperty property : properties) {
            AnnotationValue<io.micronaut.data.cosmos.annotation.PartitionKey> partitionKeyAnnotationValue = property.getAnnotation(io.micronaut.data.cosmos.annotation.PartitionKey.class);
            if (partitionKeyAnnotationValue != null) {
                if (StringUtils.isNotEmpty(partitionKeyPath)) {
                    throw new IllegalStateException("Multiple @PartitionKey annotations declared on " + entity.getName() + ". Azure Cosmos DB supports only one partition key.");
                }
                String partitionKeyValue = partitionKeyAnnotationValue.stringValue("value").orElse("");
                if (StringUtils.isNotEmpty(partitionKeyValue)) {
                    partitionKeyPath = partitionKeyValue;
                } else {
                    partitionKeyPath = property.getPersistedName();
                }
            }
        }
        return partitionKeyPath;
    }

    private ThroughputProperties createThroughputProperties(CosmosDatabaseConfiguration configuration) {
        // Throughput properties for the database
        if (configuration.getThroughputRequestUnits() != null) {
            if (configuration.isThroughputAutoScale()) {
                return ThroughputProperties.createAutoscaledThroughput(configuration.getThroughputRequestUnits());
            } else {
                return ThroughputProperties.createManualThroughput(configuration.getThroughputRequestUnits());
            }
        }
        return null;
    }

    @Override
    public <T> T findOne(Class<T> type, Serializable id) {
        RuntimePersistentEntity<T> persistentEntity = runtimeEntityRegistry.getEntity(type);
        CosmosContainer container = getContainer(persistentEntity);
        try {
            final SqlParameter param = new SqlParameter("@ROOT_ID", getStringIdValue(id));
            final SqlQuerySpec querySpec = new SqlQuerySpec(FIND_ONE_DEFAULT_QUERY, param);
            logQuery(querySpec, Arrays.asList(param));
            final CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
            CosmosPagedIterable<ObjectNode> result = container.queryItems(querySpec, options, ObjectNode.class);
            Iterator<ObjectNode> iterator = result.iterator();
            if (iterator.hasNext()) {
                ObjectNode beanTree = iterator.next();
                if (iterator.hasNext()) {
                    throw new NonUniqueResultException();
                }
                return deserializeFromTree(beanTree, Argument.of(type));
            }
        } catch (CosmosException e) {
            if (e.getStatusCode() == NOT_FOUND_STATUS_CODE) {
                return null;
            }
            throw e;
        }
        return null;
    }

    @Override
    public <T, R> R findOne(PreparedQuery<T, R> preparedQuery) {
        RuntimePersistentEntity<T> persistentEntity = runtimeEntityRegistry.getEntity(preparedQuery.getRootEntity());
        CosmosContainer container = getContainer(persistentEntity);
        List<SqlParameter> paramList = bindParameters(preparedQuery);
        SqlQuerySpec querySpec = new SqlQuerySpec(preparedQuery.getQuery(), paramList);
        logQuery(querySpec, paramList);
        CosmosPagedIterable<ObjectNode> result = container.queryItems(querySpec, new CosmosQueryRequestOptions(), ObjectNode.class);
        Iterator<ObjectNode> iterator = result.iterator();
        if (iterator.hasNext()) {
            ObjectNode beanTree = iterator.next();
            if (iterator.hasNext()) {
                throw new NonUniqueResultException();
            }
            if (preparedQuery.isDtoProjection()) {
                Class<R> wrapperType = ReflectionUtils.getWrapperType(preparedQuery.getResultType());
                return deserializeFromTree(beanTree, Argument.of(wrapperType));
            } else {
                return deserializeFromTree(beanTree, Argument.of((Class<R>) preparedQuery.getRootEntity()));
            }
        }
        return null;
    }

    @Override
    public <T> boolean exists(PreparedQuery<T, Boolean> preparedQuery) {
        RuntimePersistentEntity<T> persistentEntity = runtimeEntityRegistry.getEntity(preparedQuery.getRootEntity());
        CosmosContainer container = getContainer(persistentEntity);
        List<SqlParameter> paramList = bindParameters(preparedQuery);
        SqlQuerySpec querySpec = new SqlQuerySpec(preparedQuery.getQuery(), paramList);
        logQuery(querySpec, paramList);
        CosmosPagedIterable<ObjectNode> result = container.queryItems(querySpec, new CosmosQueryRequestOptions(), ObjectNode.class);
        Iterator<ObjectNode> iterator = result.iterator();
        if (iterator.hasNext()) {
            return true;
        }
        return false;
    }

    @Override
    public <T> Iterable<T> findAll(PagedQuery<T> query) {
        return null;
    }

    @Override
    public <T> long count(PagedQuery<T> pagedQuery) {
        return 0;
    }

    @Override
    public <T, R> Iterable<R> findAll(PreparedQuery<T, R> preparedQuery) {
        try (Stream<R> stream = findStream(preparedQuery)) {
            return stream.collect(Collectors.toList());
        }
    }

    @Override
    public <T, R> Stream<R> findStream(PreparedQuery<T, R> preparedQuery) {
        AtomicBoolean finished = new AtomicBoolean();
        RuntimePersistentEntity<T> persistentEntity = runtimeEntityRegistry.getEntity(preparedQuery.getRootEntity());
        Class<R> resultType = preparedQuery.getResultType();
        CosmosContainer container = getContainer(persistentEntity);
        List<SqlParameter> paramList = bindParameters(preparedQuery);
        SqlQuerySpec querySpec = new SqlQuerySpec(preparedQuery.getQuery(), paramList);
        logQuery(querySpec, paramList);
        try {
            Spliterator<R> spliterator;
            boolean dtoProjection = preparedQuery.isDtoProjection();
            boolean isEntity = preparedQuery.getResultDataType() == DataType.ENTITY;
            if (isEntity || dtoProjection) {
                Argument<R> argument;
                if (dtoProjection) {
                    argument = Argument.of(ReflectionUtils.getWrapperType(preparedQuery.getResultType()));
                } else {
                    argument = Argument.of((Class<R>) preparedQuery.getRootEntity());
                }
                CosmosPagedIterable<ObjectNode> result = container.queryItems(querySpec, new CosmosQueryRequestOptions(), ObjectNode.class);
                Iterator<ObjectNode> iterator = result.iterator();
                spliterator = new Spliterators.AbstractSpliterator<R>(Long.MAX_VALUE,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE) {
                    @Override
                    public boolean tryAdvance(Consumer<? super R> action) {
                        if (finished.get()) {
                            return false;
                        }
                        boolean hasNext = iterator.hasNext();
                        if (hasNext) {
                            ObjectNode beanTree = iterator.next();
                            R o;
                            if (dtoProjection) {
                                o = deserializeFromTree(beanTree, argument);
                            } else {
                                o = deserializeFromTree(beanTree, argument);
                            }
                            action.accept(o);
                        } else {
                            finished.set(true);
                        }
                        return hasNext;
                    }
                };
            } else {
                DataType dataType = preparedQuery.getResultDataType();
                CosmosPagedIterable<?> result = container.queryItems(querySpec, new CosmosQueryRequestOptions(), getDataTypeClass(dataType));
                Iterator<?> iterator = result.iterator();
                spliterator = new Spliterators.AbstractSpliterator<R>(Long.MAX_VALUE,
                    Spliterator.ORDERED | Spliterator.IMMUTABLE) {
                    @Override
                    public boolean tryAdvance(Consumer<? super R> action) {
                        if (finished.get()) {
                            return false;
                        }
                        try {
                            boolean hasNext = iterator.hasNext();
                            if (hasNext) {
                                Object v = iterator.next();
                                if (resultType.isInstance(v)) {
                                    //noinspection unchecked
                                    action.accept((R) v);
                                } else if (v != null) {
                                    Object r = ConversionService.SHARED.convertRequired(v, resultType);
                                    if (r != null) {
                                        action.accept((R) r);
                                    }
                                }
                            } else {
                                finished.set(true);
                            }
                            return hasNext;
                        } catch (Exception e) {
                            throw new DataAccessException("Error retrieving next Cosmos result: " + e.getMessage(), e);
                        }
                    }
                };
            }
            return StreamSupport.stream(spliterator, false).onClose(() -> {
                finished.set(true);
            });
        } catch (Exception e) {
            throw new DataAccessException("Cosmos SQL Error executing Query: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> Stream<T> findStream(PagedQuery<T> query) {
        return null;
    }

    @Override
    public <R> Page<R> findPage(PagedQuery<R> query) {
        return null;
    }

    @Override
    public <T> T persist(InsertOperation<T> operation) {
        CosmosContainer container = getContainer(operation);
        T entity = operation.getEntity();
        RuntimePersistentEntity persistentEntity = runtimeEntityRegistry.getEntity(entity.getClass());
        ObjectNode tree = serializeToTree(entity, Argument.of(operation.getRootEntity()));
        PartitionKey partitionKey = PartitionKey.NONE;
        CosmosContainerProperties props = cosmosEntityProperties.get(persistentEntity);
        String partitionKeyPath = props != null ? props.getPartitionKeyPath() : null;
        if (StringUtils.isNotEmpty(partitionKeyPath)) {
            JsonNode partitionKeyObjValue = tree.get(partitionKeyPath);
            if (partitionKeyObjValue != null) {
                partitionKey = new PartitionKey(partitionKeyObjValue.asText());
            }
        }
        container.createItem(tree, partitionKey, new CosmosItemRequestOptions());
        return entity;
    }

    @Override
    public <T> T update(UpdateOperation<T> operation) {
        return null;
    }

    @Override
    public Optional<Number> executeUpdate(PreparedQuery<?, Number> preparedQuery) {
        return Optional.empty();
    }

    @Override
    public <T> int delete(DeleteOperation<T> operation) {
        return 0;
    }

    @Override
    public <T> Optional<Number> deleteAll(DeleteBatchOperation<T> operation) {
        return Optional.empty();
    }

    private <T, R> List<SqlParameter> bindParameters(PreparedQuery<T, R> preparedQuery) {
        List<SqlParameter> paramList = new ArrayList<>();
        SqlPreparedQuery<T, R> sqlPreparedQuery = getSqlPreparedQuery(preparedQuery);
        sqlPreparedQuery.bindParameters(new SqlStoredQuery.Binder() {

            @Override
            public Object autoPopulateRuntimeProperty(RuntimePersistentProperty<?> persistentProperty, Object previousValue) {
                return runtimeEntityRegistry.autoPopulateRuntimeProperty(persistentProperty, previousValue);
            }

            @Override
            public Object convert(Object value, RuntimePersistentProperty<?> property) {
                AttributeConverter<Object, Object> converter = property.getConverter();
                if (converter != null) {
                    return converter.convertToPersistedValue(value, createTypeConversionContext(property, property.getArgument()));
                }
                return value;
            }

            @Override
            public Object convert(Class<?> converterClass, Object value, Argument<?> argument) {
                if (converterClass == null) {
                    return value;
                }
                AttributeConverter<Object, Object> converter = attributeConverterRegistry.getConverter(converterClass);
                ConversionContext conversionContext = createTypeConversionContext(null, argument);
                return converter.convertToPersistedValue(value, conversionContext);
            }

            private ConversionContext createTypeConversionContext(@Nullable RuntimePersistentProperty<?> property,
                                                                  @Nullable Argument<?> argument) {
                if (property != null) {
                    return ConversionContext.of(property.getArgument());
                }
                if (argument != null) {
                    return ConversionContext.of(argument);
                }
                return ConversionContext.DEFAULT;
            }

            @Override
            public void bindOne(QueryParameterBinding binding, Object value) {
                paramList.add(new SqlParameter("@" + binding.getRequiredName(), value));
            }

            @Override
            public void bindMany(QueryParameterBinding binding, Collection<Object> values) {
                bindOne(binding, values);
            }

            @Override
            public int currentIndex() {
                return 0;
            }

        }); return paramList;
    }

    @Override
    public <E, R> PreparedQuery<E, R> decorate(PreparedQuery<E, R> preparedQuery) {
        return new DefaultSqlPreparedQuery<>(preparedQuery);
    }

    @Override
    public <E, R> StoredQuery<E, R> decorate(MethodInvocationContext<?, ?> context, StoredQuery<E, R> storedQuery) {
        SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
        RuntimePersistentEntity<E> runtimePersistentEntity = runtimeEntityRegistry.getEntity(storedQuery.getRootEntity());
        return new DefaultSqlStoredQuery<>(storedQuery, runtimePersistentEntity, queryBuilder);
    }

    private void createContainer(CosmosDatabase cosmosDatabase, PersistentEntity persistentEntity) {
        String containerName = persistentEntity.getPersistedName();
        CosmosContainerProperties props = cosmosEntityProperties.get(persistentEntity);
        if (props != null) {
            containerName = props.getContainerName();
        }
        String partitionKey = getPartitionKey(props);
        com.azure.cosmos.models.CosmosContainerProperties containerProperties =
            new com.azure.cosmos.models.CosmosContainerProperties(containerName, partitionKey);
        ThroughputProperties throughputProperties = props != null ? props.getThroughputProperties() : null;
        if (throughputProperties == null) {
            cosmosDatabase.createContainerIfNotExists(containerProperties);
        } else {
            cosmosDatabase.createContainerIfNotExists(containerProperties, throughputProperties);
        }
    }

    private <T> CosmosContainer getContainer(InsertOperation<T> operation) {
        RuntimePersistentEntity<T> persistentEntity = runtimeEntityRegistry.getEntity(operation.getRootEntity());
        CosmosContainer container = getContainer(database, persistentEntity);
        return container;
    }

    private <T> CosmosContainer getContainer(RuntimePersistentEntity<T> persistentEntity) {
        return getContainer(database, persistentEntity);
    }

    private CosmosContainer getContainer(CosmosDatabase cosmosDatabase, RuntimePersistentEntity<?> persistentEntity) {
        String containerName = persistentEntity.getPersistedName();
        CosmosContainerProperties props = cosmosEntityProperties.get(persistentEntity);
        if (props != null) {
            containerName = props.getContainerName();
        }
        String partitionKey = getPartitionKey(props);
        com.azure.cosmos.models.CosmosContainerProperties containerProperties =
            new com.azure.cosmos.models.CosmosContainerProperties(containerName, partitionKey);
        ThroughputProperties throughputProperties = props == null ? null : props.getThroughputProperties();
        CosmosContainerResponse containerResponse;
        if (throughputProperties == null) {
            containerResponse = cosmosDatabase.createContainerIfNotExists(containerProperties);
        } else {
            containerResponse = cosmosDatabase.createContainerIfNotExists(containerProperties, throughputProperties);
        }
        return cosmosDatabase.getContainer(containerResponse.getProperties().getId());
    }

    private String getPartitionKey(CosmosContainerProperties props) {
        if (props != null && StringUtils.isNotEmpty(props.getPartitionKeyPath())) {
            return "/" + props.getPartitionKeyPath();
        }
        return "/null";
    }

    private ObjectNode serializeToTree(Object bean, Argument<?> type) {
        try {
            Serializer.EncoderContext encoderContext = serdeRegistry.newEncoderContext(null);
            Serializer<? super Object> typeSerializer = serdeRegistry.findSerializer(type);
            Serializer<Object> serializer = typeSerializer.createSpecific(encoderContext, type);
            JsonNodeEncoder encoder = JsonNodeEncoder.create();
            serializer.serialize(encoder, encoderContext, type, bean);
            // First serialize to Micronaut Serde tree model and then convert it to Jackson's tree model
            io.micronaut.json.tree.JsonNode jsonNode = encoder.getCompletedValue();
            try (JsonParser jsonParser = JsonNodeTreeCodec.getInstance().treeAsTokens(jsonNode)) {
                return objectMapper.readTree(jsonParser);
            }
        } catch (IOException e) {
            throw new DataAccessException("Failed to serialize: " + e.getMessage(), e);
        }
    }

    private <T> T deserializeFromTree(ObjectNode objectNode, Argument<T> type) {
        try {
            Deserializer.DecoderContext decoderContext = serdeRegistry.newDecoderContext(null);
            Deserializer<? extends T> typeDeserializer = serdeRegistry.findDeserializer(type);
            Deserializer<? extends T> deserializer = typeDeserializer.createSpecific(decoderContext, type);
            JsonParser parser = objectNode.traverse();
            if (!parser.hasCurrentToken()) {
                parser.nextToken();
            }
            final Decoder decoder = JacksonDecoder.create(parser, Object.class);
            return deserializer.deserialize(decoder, decoderContext, type);
        } catch (IOException e) {
            throw new DataAccessException("Failed to deserialize: " + e.getMessage(), e);
        }
    }

    private <E, R> SqlPreparedQuery<E, R> getSqlPreparedQuery(PreparedQuery<E, R> preparedQuery) {
        if (preparedQuery instanceof SqlPreparedQuery) {
            return (SqlPreparedQuery<E, R>) preparedQuery;
        }
        throw new IllegalStateException("Expected for prepared query to be of type: SqlPreparedQuery got: " + preparedQuery.getClass().getName());
    }

    private <E, R> SqlStoredQuery<E, R> getSqlStoredQuery(StoredQuery<E, R> storedQuery) {
        if (storedQuery instanceof SqlStoredQuery) {
            SqlStoredQuery<E, R> sqlStoredQuery = (SqlStoredQuery<E, R>) storedQuery;
            if (sqlStoredQuery.isExpandableQuery() && !(sqlStoredQuery instanceof SqlPreparedQuery)) {
                return new DefaultSqlPreparedQuery<>(sqlStoredQuery);
            }
            return sqlStoredQuery;
        }
        throw new IllegalStateException("Expected for prepared query to be of type: SqlStoredQuery got: " + storedQuery.getClass().getName());
    }

    private void logQuery(SqlQuerySpec querySpec, Iterable<SqlParameter> params) {
        if (QUERY_LOG.isDebugEnabled()) {
            QUERY_LOG.debug("Executing query: {}", querySpec.getQueryText());
            for (SqlParameter param : params) {
                QUERY_LOG.debug("Parameter: name={}, value={}", param.getName(), param.getValue(Object.class));
            }
        }
    }

    public static String getStringIdValue(Object idValue) {
        ArgumentUtils.requireNonNull("idValue", idValue);
        if (idValue instanceof String) {
            String strValue = idValue.toString();
            ArgumentUtils.check("idValue", new ArgumentUtils.Check() {
                @Override
                public boolean condition() {
                    return StringUtils.isNotEmpty(strValue);
                }
            });
            return strValue;
        } else if (idValue instanceof Integer) {
            return Integer.toString((Integer) idValue);
        } else if (idValue instanceof Long) {
            return Long.toString((Long) idValue);
        } else if (idValue instanceof UUID) {
            return idValue.toString();
        } else {
            throw new IllegalArgumentException("Type of id field must be String or Integer or Long or UUID");
        }
    }

    Class<?> getDataTypeClass(DataType dataType) {
        switch (dataType) {
            case STRING:
            case JSON:
                return String.class;
            case UUID:
                return UUID.class;
            case LONG:
                return Long.class;
            case INTEGER:
                return Integer.class;
            case BOOLEAN:
                return Boolean.class;
            case BYTE:
                return Byte.class;
            case TIMESTAMP:
                return Date.class;
            case DATE:
                return Date.class;
            case CHARACTER:
                return Character.class;
            case FLOAT:
                return Float.class;
            case SHORT:
                return Short.class;
            case DOUBLE:
                return Double.class;
            case BYTE_ARRAY:
                return byte[].class;
            case BIGDECIMAL:
                return BigDecimal.class;
            case OBJECT:
            default:
                return Object.class;
        }
    }
}
