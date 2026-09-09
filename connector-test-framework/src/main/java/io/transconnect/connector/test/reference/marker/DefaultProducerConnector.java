/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference.marker;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.test.reference.ReferenceTest;

/**
 * Marker connector for the {@link ReferenceTest} annotation.
 */
public class DefaultProducerConnector implements ProducerConnector {

    /**
     * Always returns {@code null} - this marker connector is never instantiated by a test.
     *
     * @param context ignored
     * @return {@code null}
     */
    @Override
    public ProducerConnection createConnection(Context context) {
        return null;
    }

    /**
     * Always returns {@code null} - this marker connector is never instantiated by a test.
     *
     * @return {@code null}
     */
    @Override
    public ProducerConnectorDescriptor getDescription() {
        return null;
    }
}
