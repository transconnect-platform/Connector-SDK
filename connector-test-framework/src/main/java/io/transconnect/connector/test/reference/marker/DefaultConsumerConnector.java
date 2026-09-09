/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference.marker;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.test.reference.ReferenceTest;

/**
 * Marker connector for the {@link ReferenceTest} annotation.
 */
public class DefaultConsumerConnector implements ConsumerConnector {

    /**
     * Always returns {@code null} - this marker connector is never instantiated by a test.
     *
     * @param context ignored
     * @return {@code null}
     */
    @Override
    public ConsumerConnection createConnection(Context context) {
        return null;
    }

    /**
     * Always returns {@code null} - this marker connector is never instantiated by a test.
     *
     * @return {@code null}
     */
    @Override
    public ConsumerConnectorDescriptor getDescription() {
        return null;
    }
}
