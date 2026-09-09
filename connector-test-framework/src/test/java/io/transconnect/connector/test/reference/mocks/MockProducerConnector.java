/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference.mocks;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;

/**
 * MockProducer Connector for testing the test framework itself.
 */
public class MockProducerConnector implements ProducerConnector {

    /**
     * create a producer connection.
     * @param context the context information like the configuration and extensions that must be used
     * @return ProducerConnection
     */
    @Override
    public ProducerConnection createConnection(Context context) {
        ProducerConnection connection = mock(ProducerConnection.class);
        doReturn(true).when(connection).isConnected();
        return connection;
    }

    /**
     * gets a Producer Connector Descriptor with test values.
     * @return Producer Connector Descriptor
     */
    @Override
    public ProducerConnectorDescriptor getDescription() {
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
        return (new ProducerConnectorDescriptor())
                .toBuilder().properties(properties).build();
    }
}
