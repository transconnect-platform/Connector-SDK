/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.consumer;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.IConnector;

/**
 * A Connector that is actively called by the TRANSCONNECT runtime to process a Message. The connector must
 * return a Message with the processed result.
 */
public interface ConsumerConnector extends IConnector<ConsumerConnectorDescriptor> {

    /**
     * Create a new connection for the given configuration.
     *
     * @param context context information like the configuration and extensions that must be used to
     * create and handle the connection.
     * @return a new {@link ConsumerConnection}
     */
    ConsumerConnection createConnection(Context context);
}
