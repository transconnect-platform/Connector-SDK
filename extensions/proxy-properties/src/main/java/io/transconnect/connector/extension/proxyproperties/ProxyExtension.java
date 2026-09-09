/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.proxyproperties;

import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.extension.DescriptorExtension;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import lombok.Getter;

/**
 * This extension adds proxy properties to the connector description.
 * <p>
 * The properties are:
 * <ul>
 *     <li>Proxy Host</li>
 *     <li>Proxy Port</li>
 *     <li>Proxy Username</li>
 *     <li>Proxy Password</li>
 * </ul>
 */
@Getter
public class ProxyExtension implements DescriptorExtension {

    /**
     * ID of the property holding the proxy host name.
     */
    public static final String PROPERTY_PROXY_HOST = "$extension.proxy.host";

    /**
     * ID of the property holding the proxy port.
     */
    public static final String PROPERTY_PROXY_PORT = "$extension.proxy.port";

    /**
     * ID of the property holding the proxy user name.
     */
    public static final String PROPERTY_PROXY_USER = "$extension.proxy.user";

    /**
     * ID of the property holding the proxy password.
     */
    public static final String PROPERTY_PROXY_PASSWORD = "$extension.proxy.password";

    /**
     * the configured proxy host name.
     *
     * @return the configured proxy host name, or {@code null} if none is configured
     */
    private String host;

    /**
     * the configured proxy port.
     *
     * @return the configured proxy port, or {@code null} if none is configured
     */
    private Long port;

    /**
     * the configured proxy user name.
     *
     * @return the configured proxy user name, or {@code null} if none is configured
     */
    private String username;

    /**
     * the configured proxy password.
     *
     * @return the configured proxy password, or {@code null} if none is configured
     */
    private String password;

    /**
     * {@inheritDoc}
     *
     * <p>Returns a copy of the given descriptor with the proxy properties appended.
     */
    @Override
    public ConsumerConnectorDescriptor modifyDescription(ConsumerConnectorDescriptor description) {
        return description.toBuilder()
                .properties(addProxyProperties(description.getProperties()))
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a copy of the given descriptor with the proxy properties appended.
     */
    @Override
    public ProducerConnectorDescriptor modifyDescription(ProducerConnectorDescriptor description) {
        return description.toBuilder()
                .properties(addProxyProperties(description.getProperties()))
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads the proxy properties from the connector configuration and makes them available
     * through the getters of this extension.
     */
    @Override
    public void readConfiguration(Configuration configuration) {
        configuration.getString(PROPERTY_PROXY_HOST).ifPresent(value -> host = value);
        configuration.getLong(PROPERTY_PROXY_PORT).ifPresent(value -> port = value);
        configuration.getString(PROPERTY_PROXY_USER).ifPresent(value -> username = value);
        configuration.getSecret(PROPERTY_PROXY_PASSWORD).ifPresent(value -> password = value);
    }

    private IConnectorProperty[] addProxyProperties(IConnectorProperty[] properties) {
        var propertyList = new ArrayList<>(Arrays.asList(properties));
        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_PROXY_HOST)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Proxy-Host"), LocalizedText.of(Locale.ENGLISH, "Proxy Host")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Der Host-Name des Proxy-Servers."),
                    LocalizedText.of(Locale.ENGLISH, "The host name of the proxy server.")
                })
                .type(ConnectorPropertyType.TEXT)
                .build());
        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_PROXY_PORT)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Proxy-Port"), LocalizedText.of(Locale.ENGLISH, "Proxy Port")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Der Port des Proxy-Servers."),
                    LocalizedText.of(Locale.ENGLISH, "The port of the proxy server.")
                })
                .type(ConnectorPropertyType.INTEGER)
                .build());
        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_PROXY_USER)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Proxy Benutzername"),
                    LocalizedText.of(Locale.ENGLISH, "Proxy Username")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Der Benutzername für die Anmeldung am Proxy-Server."),
                    LocalizedText.of(Locale.ENGLISH, "The user name for authentication to the proxy server.")
                })
                .type(ConnectorPropertyType.TEXT)
                .build());
        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_PROXY_PASSWORD)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Proxy Passwort"),
                    LocalizedText.of(Locale.ENGLISH, "Proxy Password")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Passwort für die Anmeldung am Proxy-Server."),
                    LocalizedText.of(Locale.ENGLISH, "Password for authentication to the proxy server.")
                })
                .type(ConnectorPropertyType.SECRET)
                .build());
        return propertyList.toArray(new IConnectorProperty[0]);
    }
}
