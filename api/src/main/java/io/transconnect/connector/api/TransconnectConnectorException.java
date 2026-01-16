/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

/**
 * Checked Exception from a connector that indicate a problem with the connector logic.
 */
public class TransconnectConnectorException extends Exception {

    /**
     * Constructor that can be pass a message.
     *
     * @param message the error message
     */
    public TransconnectConnectorException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param message the error message
     * @param cause the cause error
     */
    public TransconnectConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}
