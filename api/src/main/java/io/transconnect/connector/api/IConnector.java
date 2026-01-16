/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

/**
 * Interface for a connector that provides a description of its capabilities.
 *
 * @param <T> the type of the connector description
 */
public interface IConnector<T extends ConnectorDescriptor> {

    /**
     * Returns the description of the connector.
     *
     * @return an instance of {@link ConnectorDescriptor}
     */
    T getDescription();
}
