/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.oauth2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.api.TransconnectConnectorException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MicrosoftOAuth2TokenProviderExtensionTest {

    private MicrosoftOAuth2TokenProviderExtension extension;

    @BeforeEach
    void setUp() {
        extension = new MicrosoftOAuth2TokenProviderExtension();
    }

    @Test
    void initialize_withValidConfiguration_succeeds() throws Exception {
        var config = createValidConfig();
        extension.readConfiguration(config);
        assertTrue(extension.isInitialized());
    }

    @Test
    void initialize_withCustomScope_succeeds() throws Exception {
        var config = createValidConfig();
        config.setValue(
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_SCOPE, "https://graph.microsoft.com/.default");
        extension.readConfiguration(config);
        assertTrue(extension.isInitialized());
    }

    @ParameterizedTest
    @MethodSource("invalidConfigurations")
    void initialize_withInvalidConfiguration_throwsException(MockConfig config) {
        assertThrows(TransconnectConnectorException.class, () -> extension.readConfiguration(config));
    }

    @Test
    void accessMethods_withoutInitialization_throwException() {
        assertThrows(TransconnectConnectorException.class, () -> extension.getAccessToken());
        assertThrows(TransconnectConnectorException.class, () -> extension.getClientApp());
    }

    @Test
    void isInitialized_initiallyFalse() {
        assertFalse(extension.isInitialized());
    }

    // Method sources
    private static Stream<MockConfig> invalidConfigurations() {
        return Stream.of(
                createConfigMissing(OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL),
                createConfigMissing(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID),
                createConfigMissing(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET),
                createConfigWithInvalidUrl(),
                new MockConfig() // Empty config
                );
    }

    // Helper methods
    private static MockConfig createValidConfig() {
        var config = new MockConfig();
        config.setValue(
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL,
                "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token");
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID, "test-client-id");
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET, "test-client-secret");
        return config;
    }

    private static MockConfig createConfigMissing(String propertyToExclude) {
        var config = new MockConfig();

        // Add all valid properties except the excluded one
        if (!propertyToExclude.equals(OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL)) {
            config.setValue(
                    OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL,
                    "https://login.microsoftonline.com/test-tenant/oauth2/v2.0/token");
        }
        if (!propertyToExclude.equals(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID)) {
            config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID, "test-client-id");
        }
        if (!propertyToExclude.equals(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET)) {
            config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET, "test-client-secret");
        }

        return config;
    }

    private static MockConfig createConfigWithInvalidUrl() {
        var config = createValidConfig();
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL, "invalid-url");
        return config;
    }

    @FunctionalInterface
    private interface AccessMethod {
        Object call(MicrosoftOAuth2TokenProviderExtension extension) throws TransconnectConnectorException;
    }
}