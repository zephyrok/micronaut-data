package io.micronaut.data.runtime.multitenancy.conf;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.data.runtime.config.DataSettings;
import io.micronaut.data.runtime.multitenancy.MultiTenancyMode;

@ConfigurationProperties(MultiTenancyConfiguration.PREFIX)
public final class MultiTenancyConfiguration {

    /**
     * Prefix for config.
     */
    static final String PREFIX = DataSettings.PREFIX + ".multi-tenancy";

    private MultiTenancyMode mode;

    public MultiTenancyMode getMode() {
        return mode;
    }

    public void setMode(MultiTenancyMode mode) {
        this.mode = mode;
    }
}
