/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.api.mocks;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;

/**
 * MockConnector without extenstion
 */
public class MockConnectorWithoutExtensions implements ConsumerConnector {
    @Override
    public ConsumerConnection createConnection(Context context) {
        return null;
    }

    @Override
    public ConsumerConnectorDescriptor getDescription() {
        return null;
    }
}
