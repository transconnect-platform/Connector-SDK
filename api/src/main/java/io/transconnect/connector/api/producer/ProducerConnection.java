/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.producer;

import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.message.MessageFactory;
import io.transconnect.connector.api.message.WritableMessage;
import java.io.Closeable;

/**
 * A connection from a producing connector. It creates new messages after the connect and informs the calling code.
 */
public interface ProducerConnection extends Closeable {

    /**
     * Is called everytime the isConnected method returns false. In this method the
     * connection is prepared if necessary. This method should return when the connection is established. It must not
     * block until messages are produced.
     *
     * @param factory The message factory used to generate new messages
     * @param listener Acts as the message receiver of the generated messages and gets notified when this connection is
     * disconnected. This listener needs to be thread-safe, the connector might call any method multiple times from
     * different thread contexts.
     * @throws TransconnectConnectorException if there was an error during establishing the connection.
     */
    void connect(MessageFactory<WritableMessage> factory, ProducerConnectorListener listener)
            throws TransconnectConnectorException;

    /**
     * Returns the connection state. If the method returns false, the connect method needs to be called until this
     * connection will produce messages again.
     *
     * @return true if the connection is established, otherwise false
     */
    boolean isConnected();
}
