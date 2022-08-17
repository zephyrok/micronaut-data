package io.micronaut.data.runtime.multitenancy.internal;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.runtime.multitenancy.TenantResolver;
import jakarta.inject.Singleton;

import java.io.Serializable;

@Internal
@Singleton
@Requires(missingBeans = TenantResolver.class, classes = io.micronaut.multitenancy.tenantresolver.TenantResolver.class)
class DefaultTenantResolver implements TenantResolver {

    private final io.micronaut.multitenancy.tenantresolver.TenantResolver tenantResolver;

    DefaultTenantResolver(io.micronaut.multitenancy.tenantresolver.TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public Serializable resolveTenantIdentifier() {
        Serializable tenantId = tenantResolver.resolveTenantIdentifier();
        if (tenantId == io.micronaut.multitenancy.tenantresolver.TenantResolver.DEFAULT) {
            return null;
        }
        return tenantId;
    }
}
