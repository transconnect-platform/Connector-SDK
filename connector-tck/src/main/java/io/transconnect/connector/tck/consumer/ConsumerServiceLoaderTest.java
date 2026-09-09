/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck.consumer;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.tck.AbstractServiceLoaderTest;

/**
 * Runs the TCK {@link java.util.ServiceLoader} checks for {@link ConsumerConnector} implementations.
 * Extend this class in your connector project to verify that your connector is discoverable and
 * annotated with {@link io.transconnect.connector.api.extension.Connector}.
 */
public class ConsumerServiceLoaderTest extends AbstractServiceLoaderTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getConnectorClass() {
        return ConsumerConnector.class;
    }
}
