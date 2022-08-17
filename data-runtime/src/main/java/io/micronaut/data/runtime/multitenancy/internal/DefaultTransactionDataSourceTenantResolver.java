package io.micronaut.data.runtime.multitenancy.internal;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.runtime.multitenancy.DataSourceTenantResolver;
import io.micronaut.transaction.interceptor.TransactionDataSourceTenantResolver;
import jakarta.inject.Singleton;

@Internal
@Singleton
@Requires(beans = DataSourceTenantResolver.class)
final class DefaultTransactionDataSourceTenantResolver implements TransactionDataSourceTenantResolver {

    private final DataSourceTenantResolver tenantResolver;

    DefaultTransactionDataSourceTenantResolver(DataSourceTenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public String resolveTenantDataSourceName() {
        return tenantResolver.resolveTenantDataSourceName();
    }
}
