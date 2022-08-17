package io.micronaut.data.runtime.multitenancy.internal;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.runtime.multitenancy.conf.DataSourceMultiTenancyEnabledCondition;
import io.micronaut.data.runtime.multitenancy.DataSourceTenantResolver;
import io.micronaut.data.runtime.multitenancy.TenantResolver;
import jakarta.inject.Singleton;

import java.io.Serializable;

@Singleton
@Requires(
    condition = DataSourceMultiTenancyEnabledCondition.class,
    beans = TenantResolver.class,
    missingBeans = DataSourceTenantResolver.class
)
public class DefaultDataSourceTenantResolver implements DataSourceTenantResolver {

    private final TenantResolver tenantResolver;

    public DefaultDataSourceTenantResolver(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public String resolveTenantDataSourceName() {
        Serializable tenantId = tenantResolver.resolveTenantIdentifier();
        return tenantId == null ? null : tenantId.toString();
    }

}
