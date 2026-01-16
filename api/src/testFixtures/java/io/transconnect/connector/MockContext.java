/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector;

import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.extension.ConnectorExtension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MockContext implements Context {

    private final Configuration config;
    private final List<ConnectorExtension> extensions;

    public MockContext(Configuration config, List<ConnectorExtension> extensions) {
        this.config = config;
        this.extensions = extensions;
    }

    public MockContext(Configuration config) {
        this(config, Collections.emptyList());
    }

    public MockContext() {
        this(new MockConfig());
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public <T extends ConnectorExtension> Optional<T> getExtension(Class<T> clazz) {
        return extensions.stream().filter(clazz::isInstance).map(clazz::cast).findFirst();
    }

    @Override
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }
}
