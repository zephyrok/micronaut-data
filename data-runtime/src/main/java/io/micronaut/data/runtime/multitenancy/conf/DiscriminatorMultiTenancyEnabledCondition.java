package io.micronaut.data.runtime.multitenancy.conf;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.runtime.multitenancy.MultiTenancyMode;

/**
 * Multi-tenancy mode {@link MultiTenancyMode#DISCRIMINATOR} condition.
 */
@Internal
public final class DiscriminatorMultiTenancyEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context) {
        return context.getBean(MultiTenancyConfiguration.class).getMode() == MultiTenancyMode.DISCRIMINATOR;
    }
}
