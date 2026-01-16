/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.consumer.yaml;

import com.example.connector.consumer.CountCharactersConnection;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.extension.yamldescriptor.ConnectorDescriptionUtils;

/**
 * Connector for counting characters in XML messages.
 * Creates character counting connections based on the configuration.
 */
public final class CountCharactersConnector implements ConsumerConnector {

    private ConsumerConnectorDescriptor description;

    /**
     * Gets the connector description from YAML configuration.
     * Loads it only once and caches for future calls.
     */
    @Override
    public ConsumerConnectorDescriptor getDescription() {
        if (description == null) {
            description = ConnectorDescriptionUtils.createConsumerDescriptionFromYaml(
                    this.getClass().getResourceAsStream("/CountCharactersConnector.yaml"));
        }
        return description;
    }

    /**
     * Creates a new connection instance with the given configuration.
     */
    @Override
    public ConsumerConnection createConnection(Context ctx) {
        return new CountCharactersConnection(ctx);
    }
}
