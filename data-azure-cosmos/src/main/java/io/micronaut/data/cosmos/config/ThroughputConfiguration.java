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
package io.micronaut.data.cosmos.config;

/**
 * Holds configuration for {@link com.azure.cosmos.models.ThroughputProperties}.
 */
public final class ThroughputConfiguration {

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
