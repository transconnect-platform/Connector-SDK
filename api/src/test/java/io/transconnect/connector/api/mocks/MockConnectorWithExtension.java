/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.api.mocks;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.extension.Connector;
import io.transconnect.connector.api.extension.Extension;

/**
 * MockConnector with given extention
 */
@Connector(extensions = @Extension(MockExtensionFirst.class))
public class MockConnectorWithExtension implements ConsumerConnector {
    @Override
    public ConsumerConnection createConnection(Context context) {
        return null;
    }

    @Override
    public ConsumerConnectorDescriptor getDescription() {
        return null;
    }
}
