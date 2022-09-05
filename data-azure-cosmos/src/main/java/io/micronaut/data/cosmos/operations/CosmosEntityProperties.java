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

import com.azure.cosmos.models.ThroughputProperties;

/**
 * The model containing values read from {@link io.micronaut.data.annotation.CosmosEntity}.
 */
public final class CosmosEntityProperties {

    private final String name;
    private final String partitionKeyPath;
    private final ThroughputProperties throughputProperties;

    public CosmosEntityProperties(String name, String partitionKeyPath, ThroughputProperties throughputProperties) {
        this.name = name;
        this.partitionKeyPath = partitionKeyPath;
        this.throughputProperties = throughputProperties;
    }

    public String getName() {
        return name;
    }

    public String getPartitionKeyPath() {
        return partitionKeyPath;
    }

    public ThroughputProperties getThroughputProperties() {
        return throughputProperties;
    }
}
