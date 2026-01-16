/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import io.transconnect.connector.api.extension.ConnectorExtension;
import java.util.Optional;

/**
 * The context of a connection.
 *
 * <p>This interface provides access to the configuration of the connector and the extensions that are
 * available for the connection.</p>
 */
public interface Context {

    /**
     * Get the configuration of the connection.
     *
     * @return The configuration of the connection
     */
    Configuration getConfiguration();

    /**
     * Gets the extension for the passed class.
     *
     * @param clazz the extension class.
     * @return the extension instance if it is present, otherwise an empty Optional
     * @param <T> the type of the extension
     */
    <T extends ConnectorExtension> Optional<T> getExtension(Class<T> clazz);

    /**
     * Gets the temporary directory as configured for the TC server.
     *
     * @return the temporary directory path
     */
    String getTempDirectory();
}
