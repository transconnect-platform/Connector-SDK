/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.producer;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.IConnector;

/**
 * A Connector that generates messages.
 */
public interface ProducerConnector extends IConnector<ProducerConnectorDescriptor> {

    /**
     * Prepares a connection for the generation of messages. The caller will handle the connection lifecycle.
     * There is no need to return a connected connection.
     *
     * @param context the context information like the configuration and extensions that must be used
     * @return The configured connection. The returned connection is not connected
     */
    ProducerConnection createConnection(Context context);
}
