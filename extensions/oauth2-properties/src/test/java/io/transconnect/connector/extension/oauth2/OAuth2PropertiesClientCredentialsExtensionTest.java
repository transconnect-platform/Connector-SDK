/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.oauth2;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OAuth2PropertiesClientCredentialsExtensionTest {

    private OAuth2PropertiesClientCredentialsExtension extension;

    @BeforeEach
    void setUp() {
        extension = new OAuth2PropertiesClientCredentialsExtension();
    }

    @ParameterizedTest
    @MethodSource("descriptorTestCases")
    void modifyDescription_addsOAuth2PropertiesWithCorrectAttributes(
            String testName, Function<OAuth2PropertiesClientCredentialsExtension, IConnectorProperty[]> getProperties) {

        // When
        IConnectorProperty[] properties = getProperties.apply(extension);
        Map<String, IConnectorProperty> propertyMap =
                Arrays.stream(properties).collect(Collectors.toMap(IConnectorProperty::getId, Function.identity()));

        // Then
        assertEquals(4, properties.length, "Should add exactly 4 OAuth2 properties");

        // Verify all required properties exist and have correct attributes
        assertOAuth2Property(
                propertyMap,
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL,
                ConnectorPropertyType.TEXT,
                true);
        assertOAuth2Property(
                propertyMap,
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID,
                ConnectorPropertyType.TEXT,
                true);
        assertOAuth2Property(
                propertyMap,
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET,
                ConnectorPropertyType.SECRET,
                true);
        assertOAuth2Property(
                propertyMap,
                OAuth2PropertiesClientCredentialsExtension.PROPERTY_SCOPE,
                ConnectorPropertyType.TEXT,
                true);
    }

    @Test
    void readConfiguration_populatesAllExtensionProperties() {
        // Given
        MockConfig config = new MockConfig();
        String expectedAuthUrl = "https://login.microsoftonline.com/tenant/oauth2/v2.0/token";
        String expectedClientId = "test-client-id";
        String expectedClientSecret = "test-client-secret";
        String expectedScope = "https://outlook.office365.com/.default";

        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL, expectedAuthUrl);
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID, expectedClientId);
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET, expectedClientSecret);
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_SCOPE, expectedScope);

        // When
        extension.readConfiguration(config);

        // Then
        assertAll(
                "All OAuth2 properties should be populated correctly",
                () -> assertEquals(expectedAuthUrl, extension.getAuthUrl()),
                () -> assertEquals(expectedClientId, extension.getClientId()),
                () -> assertEquals(expectedClientSecret, extension.getClientSecret()),
                () -> assertEquals(expectedScope, extension.getScope()));
    }

    @Test
    void readConfiguration_handlesPartialConfiguration() {
        // Given - only some properties set
        MockConfig config = new MockConfig();
        config.setValue(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID, "partial-client-id");

        // When
        extension.readConfiguration(config);

        // Then
        assertAll(
                "Should handle partial configuration",
                () -> assertNull(extension.getAuthUrl()),
                () -> assertEquals("partial-client-id", extension.getClientId()),
                () -> assertNull(extension.getClientSecret()),
                () -> assertNull(extension.getScope()));
    }

    private static Stream<Arguments> descriptorTestCases() {
        return Stream.of(
                Arguments.of("Consumer Descriptor", (Function<
                                OAuth2PropertiesClientCredentialsExtension, IConnectorProperty[]>)
                        ext -> ext.modifyDescription(ConsumerConnectorDescriptor.builder()
                                        .properties(new IConnectorProperty[0])
                                        .build())
                                .getProperties()),
                Arguments.of("Producer Descriptor", (Function<
                                OAuth2PropertiesClientCredentialsExtension, IConnectorProperty[]>)
                        ext -> ext.modifyDescription(ProducerConnectorDescriptor.builder()
                                        .properties(new IConnectorProperty[0])
                                        .build())
                                .getProperties()));
    }

    private void assertOAuth2Property(
            Map<String, IConnectorProperty> propertyMap,
            String propertyId,
            ConnectorPropertyType expectedType,
            boolean expectedRequired) {
        IConnectorProperty property = propertyMap.get(propertyId);
        assertAll(
                "OAuth2 property " + propertyId + " validation",
                () -> assertNotNull(property, "Property should exist"),
                () -> assertEquals(expectedType, property.getType(), "Property type should match"),
                () -> assertEquals(expectedRequired, property.isRequired(), "Property required flag should match"),
                () -> assertNotNull(property.getDisplayName(), "Display name should be set"),
                () -> assertTrue(property.getDisplayName().length > 0, "Display name should not be empty"));
    }
}