/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck.producer;

import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.tck.AbstractServiceLoaderTest;

/**
 * Runs the TCK {@link java.util.ServiceLoader} checks for {@link ProducerConnector} implementations.
 * Extend this class in your connector project to verify that your connector is discoverable and
 * annotated with {@link io.transconnect.connector.api.extension.Connector}.
 */
public class ProducerServiceLoaderTest extends AbstractServiceLoaderTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> getConnectorClass() {
        return ProducerConnector.class;
    }
}
