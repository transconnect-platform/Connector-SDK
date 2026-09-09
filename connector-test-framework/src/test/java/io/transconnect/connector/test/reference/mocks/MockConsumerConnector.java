/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference.mocks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;

/**
 * Consumer Connector for testing purposes
 */
public class MockConsumerConnector implements ConsumerConnector {

    /**
     * creates a mock connection
     * @param context context information like the configuration and extensions that must be used to
     * create and handle the connection.
     * @return Consumer Connection
     */
    @Override
    public ConsumerConnection createConnection(Context context) {
        ConsumerConnection connection = mock(ConsumerConnection.class);
        when(connection.isConnected()).thenReturn(true);
        try {
            doNothing().when(connection).connect();
            doNothing().when(connection).execute(any(), any(), any());
        } catch (Exception e) {
            // ignore Exception in Mock
        }
        return connection;
    }

    /**
     * provides a description for testing,
     * @return connector descriptor
     */
    @Override
    public ConsumerConnectorDescriptor getDescription() {
        // create the connector properties reference
        IConnectorProperty stringProperty = ConnectorProperty.builder()
                .id("stringTest")
                .type(ConnectorPropertyType.TEXT)
                .build();
        IConnectorProperty integerProperty = ConnectorProperty.builder()
                .id("integerTest")
                .type(ConnectorPropertyType.INTEGER)
                .build();
        IConnectorProperty floatProperty = ConnectorProperty.builder()
                .id("floatTest")
                .type(ConnectorPropertyType.FLOAT)
                .build();
        IConnectorProperty booleanProperty = ConnectorProperty.builder()
                .id("booleanTest")
                .type(ConnectorPropertyType.BOOLEAN)
                .build();
        IConnectorProperty keyProperty = ConnectorProperty.builder()
                .id("keyTest")
                .type(ConnectorPropertyType.PRIVATE_KEY)
                .build();
        IConnectorProperty certProperty = ConnectorProperty.builder()
                .id("certTest")
                .type(ConnectorPropertyType.CERTIFICATE)
                .build();

        IConnectorProperty[] properties = {
            stringProperty, integerProperty, floatProperty, booleanProperty, keyProperty, certProperty
        };
        return (new ConsumerConnectorDescriptor())
                .toBuilder().properties(properties).build();
    }
}
