/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.producer;

import io.transconnect.connector.api.message.Message;
import java.util.concurrent.Future;

/**
 * This interface is used by the producer connector to communicate produced messages and get notified when the
 * connection disconnects. Implementations of this interface need to be thread-safe. The producer connector might call
 * any method in parallel from multiple thread contexts.
 */
public interface ProducerConnectorListener {

    /**
     * Called by the connector when a new message has been produced and requires processing. The message is valid
     * according to the schema defined in the connector property 'producedMessageXsd'.
     *
     * @param message The produced message.
     * @return A Future that completes once the processing of the message is finished. The Future contains the result
     * message, which is valid according to the schema defined in the connector property 'resultMessageXsd'.
     */
    Future<Message> onMessage(Message message);

    /**
     * Called by the connector when a new message has been produced that should be enqueued for processing.
     * The message is valid according to the schema defined in the connector property 'producedMessageXsd'.
     * This method queues the message and returns immediately, without waiting for processing to complete.
     *
     * @param message The produced message.
     */
    void onMessageWithoutResult(Message message);

    /**
     * Called by the connector to signal an error.
     *
     * @param throwable The throwable that caused the error.
     */
    void onError(Throwable throwable);

    /**
     * Called by the connector to signal that the connection has been successfully established.
     */
    void onConnect();

    /**
     * Called to signal that the connection is now disconnected and will not produce any messages.
     */
    void onDisconnect();
}
