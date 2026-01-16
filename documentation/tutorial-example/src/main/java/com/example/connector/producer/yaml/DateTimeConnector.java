/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.producer.yaml;

import com.example.connector.producer.DateTimeConnection;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.extension.yamldescriptor.ConnectorDescriptionUtils;
import java.time.Clock;

/**
 * A ProducerConnector that creates periodically messages.
 */
public class DateTimeConnector implements ProducerConnector {

    /**
     * Description of the connector capabilities.
     */
    private ProducerConnectorDescriptor description;

    /**
     * Returns the connector description.
     *
     * @return the connector description
     */
    // tag::yaml-loading[]
    @Override
    public ProducerConnectorDescriptor getDescription() {

        if (description == null) {
            description = ConnectorDescriptionUtils.createProducerDescriptionFromYaml(
                    this.getClass().getResourceAsStream("/DateTimeConnector.yaml"));
        }
        return description;
    }
    // end::yaml-loading[]

    /**
     * Creates a new connection with the given configuration.
     *
     * @param ctx The context used for creating the connection.
     * @return a new connection with the given configuration from the context
     */
    @Override
    public ProducerConnection createConnection(Context ctx) {
        return new DateTimeConnection(ctx, Clock.systemDefaultZone());
    }
}
