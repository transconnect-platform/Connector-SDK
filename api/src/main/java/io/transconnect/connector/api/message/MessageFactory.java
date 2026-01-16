/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.message;

/**
 * Creates specific message instances. For the use in a connector.
 *
 * @param <T> the message type that the factory creates
 */
public interface MessageFactory<T extends Message> {

    /**
     * Creates a message instance. This is the only way a Connector should use to create a Message.
     *
     * @return a new message instance
     */
    T createMessage();
}
