/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.extension;

import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;

/**
 * Marker interface for connector extensions.
 */
public interface ConnectorExtension {
    /**
     * Read the configuration values necessary for the extension.
     *
     * @param configuration the configuration
     * @throws TransconnectConnectorException if the configuration could not be read
     */
    default void readConfiguration(Configuration configuration) throws TransconnectConnectorException {}
}
