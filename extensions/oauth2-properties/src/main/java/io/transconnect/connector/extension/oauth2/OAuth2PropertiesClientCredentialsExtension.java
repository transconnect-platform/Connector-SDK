/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.oauth2;

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
 * Extension that adds OAuth2 authentication properties to connector descriptors.
 * This extension provides standard OAuth2 configuration properties including:
 * - Authorization URL
 * - Client ID
 * - Client Secret
 * - Scope
 */
@Getter
public class OAuth2PropertiesClientCredentialsExtension implements DescriptorExtension {

    /**
     * ID of the property holding the authorization URL.
     */
    public static final String PROPERTY_AUTH_URL = "$extension.oauth2.authUrl";

    /**
     * ID of the property holding the client ID.
     */
    public static final String PROPERTY_CLIENT_ID = "$extension.oauth2.clientId";

    /**
     * ID of the property holding the client secret.
     */
    public static final String PROPERTY_CLIENT_SECRET = "$extension.oauth2.clientSecret";

    /**
     * ID of the property holding the requested scope.
     */
    public static final String PROPERTY_SCOPE = "$extension.oauth2.scope";

    /**
     * the configured authorization URL.
     *
     * @return the configured authorization URL, or {@code null} if none is configured
     */
    private String authUrl;

    /**
     * the configured client ID.
     *
     * @return the configured client ID, or {@code null} if none is configured
     */
    private String clientId;

    /**
     * the configured client secret.
     *
     * @return the configured client secret, or {@code null} if none is configured
     */
    private String clientSecret;

    /**
     * the configured scope.
     *
     * @return the configured scope, or {@code null} if none is configured
     */
    private String scope;

    /**
     * {@inheritDoc}
     *
     * <p>Returns a copy of the given descriptor with the OAuth2 client credentials properties appended.
     */
    @Override
    public ConsumerConnectorDescriptor modifyDescription(ConsumerConnectorDescriptor description) {
        return description.toBuilder()
                .properties(addOAuth2Properties(description.getProperties()))
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a copy of the given descriptor with the OAuth2 client credentials properties appended.
     */
    @Override
    public ProducerConnectorDescriptor modifyDescription(ProducerConnectorDescriptor description) {
        return description.toBuilder()
                .properties(addOAuth2Properties(description.getProperties()))
                .build();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Reads the OAuth2 client credentials properties from the connector configuration and makes them available
     * through the getters of this extension.
     */
    @Override
    public void readConfiguration(Configuration configuration) {
        configuration.getString(PROPERTY_AUTH_URL).ifPresent(value -> authUrl = value);
        configuration.getString(PROPERTY_CLIENT_ID).ifPresent(value -> clientId = value);
        configuration.getSecret(PROPERTY_CLIENT_SECRET).ifPresent(value -> clientSecret = value);
        configuration.getString(PROPERTY_SCOPE).ifPresent(value -> scope = value);
    }

    private IConnectorProperty[] addOAuth2Properties(IConnectorProperty[] properties) {
        var propertyList = new ArrayList<>(Arrays.asList(properties));

        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_AUTH_URL)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "OAuth2 Autorisierungs-URL"),
                    LocalizedText.of(Locale.ENGLISH, "OAuth2 Authorization URL")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(
                            Locale.GERMAN,
                            "Die OAuth2 Autorisierungs-Endpunkt-URL (z.B. https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token)"),
                    LocalizedText.of(
                            Locale.ENGLISH,
                            "The OAuth2 authorization endpoint URL (e.g. https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token)")
                })
                .type(ConnectorPropertyType.TEXT)
                .required(true)
                .build());

        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_CLIENT_ID)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "OAuth2 Client-ID"),
                    LocalizedText.of(Locale.ENGLISH, "OAuth2 Client ID")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Die Client-ID der registrierten Anwendung"),
                    LocalizedText.of(Locale.ENGLISH, "The client ID of the registered application")
                })
                .type(ConnectorPropertyType.TEXT)
                .required(true)
                .build());

        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_CLIENT_SECRET)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "OAuth2 Client-Secret"),
                    LocalizedText.of(Locale.ENGLISH, "OAuth2 Client Secret")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "Das Client-Secret der registrierten Anwendung"),
                    LocalizedText.of(Locale.ENGLISH, "The client secret of the registered application")
                })
                .type(ConnectorPropertyType.SECRET)
                .required(true)
                .build());

        propertyList.add(ConnectorProperty.builder()
                .id(PROPERTY_SCOPE)
                .displayName(new LocalizedText[] {
                    LocalizedText.of(Locale.GERMAN, "OAuth2 Bereiche"),
                    LocalizedText.of(Locale.ENGLISH, "OAuth2 Scopes")
                })
                .description(new LocalizedText[] {
                    LocalizedText.of(
                            Locale.GERMAN,
                            "Der OAuth2 Bereich für die Autorisierung (z.B. https://outlook.office365.com/.default). "
                                    + "Hinweis: Nur ein einzelner Bereich wird unterstützt."),
                    LocalizedText.of(
                            Locale.ENGLISH,
                            "The OAuth2 scope for authorization (e.g. https://outlook.office365.com/.default). "
                                    + "Note: Only a single scope is supported.")
                })
                .type(ConnectorPropertyType.TEXT)
                .required(true)
                .build());

        return propertyList.toArray(new IConnectorProperty[0]);
    }
}
