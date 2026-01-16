/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.consumer;

import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import java.io.Closeable;
import java.net.URI;

/**
 * Connection to a consuming connector. A consuming connector gets a message from the process that needs to be
 * processed. The Result is than returned as a new message.
 */
public interface ConsumerConnection extends Closeable {

    /**
     * Is called everytime the isConnected method returns false but a new message must be processed. In this method the
     * connection is prepared if necessary.
     *
     * @throws TransconnectConnectorException if there was an error during establishing the connection.
     */
    void connect() throws TransconnectConnectorException;

    /**
     * Returns the connection state. If the method returns false, the connect method will be called before the execute
     * method is called again.
     *
     * @return true if the connection is established, otherwise false
     */
    boolean isConnected();

    /**
     * Executes the interaction identified by the ID and with the given message.
     *
     * @param interactionId the interaction to execute.
     * @param input the Message to process
     * @param output The pre-created output message. Any result should be written to this messages
     * @throws TransconnectConnectorException if there was an error during the execution
     */
    void execute(URI interactionId, Message input, WritableMessage output) throws TransconnectConnectorException;
}
