package com.example.connector.consumer;

import io.transconnect.connector.api.CommonConnectorDescriptor;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.Interaction;
import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.extension.Connector;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.net.URI;
import java.util.Locale;

/**
 * Connector for counting characters in XML messages.
 * Creates character counting connections based on the configuration.
 */
@Connector
public final class CountCharactersConnector implements ConsumerConnector {

    private ConsumerConnectorDescriptor description;

    /**
     * Gets the connector description created from Java objects instead of YAML.
     */
    @Override
    public ConsumerConnectorDescriptor getDescription() {
        if (description == null) {
            description = createConsumerConnectorDescription();
        }
        return description;
    }

    /**
     * Creates a new connection instance with the given context.
     */
    // tag::createConnection[]
    @Override
    public ConsumerConnection createConnection(Context ctx) {
        return new CountCharactersConnection(ctx);
    }
    // end::createConnection[]

    /**
     * Creates a consumer connector description using Java objects instead of YAML.
     *
     * @return the constructed {@link io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor}
     */
    private ConsumerConnectorDescriptor createConsumerConnectorDescription() {
        // tag::description[]
        // 1. Create common connector description
        CommonConnectorDescriptor commonDescription = CommonConnectorDescriptor.builder()
                .type(URI.create("urn:transconnect:connector:count-characters-consumer"))
                .version("1.0.0")
                .vendor("SQL Projekt AG")
                .displayName(new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Count Characters Connector")})
                .description(new LocalizedText[] {
                    new LocalizedText(Locale.ENGLISH, "A simple connector for counting letters and whitespaces")
                })
                .labels(new String[] {"sample", "character-counter"})
                .build();
        // end::description[]

        // tag::property[]
        // 2. Create property for countWhitespaces
        IConnectorProperty countWhitespacesProperty = new ConnectorProperty(
                "countWhitespaces",
                new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Count Whitespaces")},
                new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Controls whether whitespaces should be counted")
                },
                ConnectorPropertyType.BOOLEAN,
                false,
                false,
                null,
                null);
        // end::property[]

        // tag::interaction[]
        // 3. Create interaction for countCharacters
        Interaction countCharactersInteraction = Interaction.builder()
                .id(URI.create("countCharacters"))
                .displayName(new LocalizedText[] {new LocalizedText(Locale.ENGLISH, "Count Characters")})
                .description(new LocalizedText[] {
                    new LocalizedText(Locale.ENGLISH, "Counts letters and whitespaces in a text")
                })
                .inMessageXsd(URI.create("/com/example/connector/consumer/in.xsd"))
                .outMessageXsd(URI.create("/com/example/connector/consumer/out.xsd"))
                .build();
        // end::interaction[]

        // tag::complete[]
        // 4. Create the consumer connector description with all components
        return ConsumerConnectorDescriptor.builder()
                .common(commonDescription)
                .properties(new IConnectorProperty[] {countWhitespacesProperty})
                .interactions(new Interaction[] {countCharactersInteraction})
                .build();
        // end::complete[]
    }
}
