package io.micronaut.data.runtime.multitenancy.internal;

import io.micronaut.context.annotation.Requires;
import io.micronaut.data.runtime.multitenancy.SchemaTenantResolver;
import io.micronaut.data.runtime.multitenancy.TenantResolver;
import io.micronaut.data.runtime.multitenancy.conf.SchemaMultiTenancyEnabledCondition;
import jakarta.inject.Singleton;

import java.io.Serializable;

@Singleton
@Requires(
    condition = SchemaMultiTenancyEnabledCondition.class,
    beans = TenantResolver.class,
    missingBeans = SchemaTenantResolver.class
)
public class DefaultSchemaTenantResolver implements SchemaTenantResolver {

    private final TenantResolver tenantResolver;

    public DefaultSchemaTenantResolver(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public String resolveTenantSchemaName() {
        Serializable tenantId = tenantResolver.resolveTenantIdentifier();
        return tenantId == null ? null : tenantId.toString();
    }
}
