package io.micronaut.data.cosmos.config;

/**
 * Holds configuration for {@link com.azure.cosmos.models.ThroughputProperties}.
 */
public class ThroughputConfiguration {

    private final boolean useThroughput;
    private final boolean manual;
    private final int throghput;

    ThroughputConfiguration(boolean useThroughput, boolean manual, int throghput) {
        this.manual = manual;
        this.throghput = throghput;
        this.useThroughput = useThroughput;
    }

    public boolean isUseThroughput() {
        return useThroughput;
    }

    public boolean isManual() {
        return manual;
    }

    public int getThroghput() {
        return throghput;
    }
}
