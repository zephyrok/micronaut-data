/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.data.cosmos.config;

import io.micronaut.context.annotation.ConfigurationProperties;

import static io.micronaut.data.cosmos.config.CosmosDatabaseConfiguration.PREFIX;

/**
 * The default Azure Cosmos database class.
 *
 * @author radovanradic
 * @since TODO
 */
@ConfigurationProperties(PREFIX)
public final class CosmosDatabaseConfiguration {

    public static final String PREFIX = "azure.cosmos.database";

    private Integer throughputRate;

    private boolean throughputAutoScale;

    private String name;

    public void setThroughputRate(int throughputRate) {
        this.throughputRate = throughputRate;
    }

    public boolean isThroughputAutoScale() {
        return throughputAutoScale;
    }

    public void setThroughputAutoScale(boolean throughputAutoScale) {
        this.throughputAutoScale = throughputAutoScale;
    }

    public ThroughputConfiguration getThroughputConfiguration() {
        return new ThroughputConfiguration(throughputRate, throughputAutoScale);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
