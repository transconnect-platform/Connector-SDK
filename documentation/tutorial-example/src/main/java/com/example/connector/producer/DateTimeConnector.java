package com.example.connector.producer;

import io.transconnect.connector.api.CommonConnectorDescriptor;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.net.URI;
import java.time.Clock;
import java.util.Locale;

/**
 * A ProducerConnector that creates periodically messages.
 */
public class DateTimeConnector implements ProducerConnector {

    /**
     * Returns the connector description.
     *
     * @return the connector description
     */
    @Override
    public ProducerConnectorDescriptor getDescription() {
        // tag::commonDescription[] 1. Create common connector description
        CommonConnectorDescriptor commonDescription = CommonConnectorDescriptor.builder()
                .type(URI.create("urn:transconnect:connector:datetime-producer"))
                .version("1.0.0")
                .vendor("SQL Projekt AG")
                .displayName(new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "DateTime Producer Connector")})
                .description(new LocalizedText[] {
                    new LocalizedText(
                            Locale.ENGLISH,
                            "The Producer Connector creates messages with a timestamp in a configurable interval")
                })
                .labels(new String[] {"sample", "producer", "DateTime"})
                .build();
        // end::commonDescription[]

        // tag::property[] 2. Create property for an interval
        IConnectorProperty intervalProperty = new ConnectorProperty(
                "interval", // ID of the property
                new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Interval")}, // Display name
                new LocalizedText[] {
                    new LocalizedText(Locale.ENGLISH, "Time interval to create new messages - Unit in seconds")
                }, // description
                ConnectorPropertyType.INTEGER, // type of the property
                60, // default value
                true, // required
                null, // we set no validators
                null // depends on no other property
                );
        // end::property[]

        // tag::description[] 3. Create the producer connector description with all components
        return ProducerConnectorDescriptor.builder()
                .common(commonDescription)
                .properties(new IConnectorProperty[] {intervalProperty})
                .resultMessageXsd(URI.create("/com/example/connector/producer/out.xsd"))
                .build();
        // end::description[]
    }

    /**
     * Creates a new connection with the given configuration.
     *
     * @param ctx The context used for creating the connection.
     * @return a new connection with the given configuration from the context
     */
    // tag::createConnection[]
    @Override
    public ProducerConnection createConnection(Context ctx) {
        return new DateTimeConnection(ctx, Clock.systemDefaultZone());
    }
    // end::createConnection[]

}
