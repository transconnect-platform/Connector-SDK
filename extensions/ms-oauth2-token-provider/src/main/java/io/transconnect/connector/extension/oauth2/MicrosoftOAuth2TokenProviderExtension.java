/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.oauth2;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.extension.ConnectorExtension;
import java.util.Collections;
import lombok.Getter;

/**
 * Microsoft OAuth2 token provider extension using MSAL for Azure/Office 365 authentication.
 *
 * Limitations:
 * - This implementation supports only a single OAuth2 scope per authentication request.
 *   Multiple scopes are not supported.
 */
public final class MicrosoftOAuth2TokenProviderExtension implements ConnectorExtension {

    private static final String DEFAULT_SCOPE = "https://outlook.office365.com/.default";

    private ConfidentialClientApplication clientApp;

    private ClientCredentialParameters clientCredentialParameters;

    private SilentParameters silentParameters;

    /**
     * whether the extension has read its configuration and is ready to hand out tokens.
     *
     * @return true if the extension is initialized, otherwise false
     */
    @Getter
    private boolean initialized = false;

    /**
     * Initializes the extension with the provided configuration.
     *
     * @param configuration The configuration containing OAuth2 properties
     * @throws TransconnectConnectorException if initialization fails
     */
    @Override
    public void readConfiguration(Configuration configuration) throws TransconnectConnectorException {
        if (initialized) {
            return;
        }

        String authUrl =
                getRequiredProperty(configuration, OAuth2PropertiesClientCredentialsExtension.PROPERTY_AUTH_URL);
        String clientId =
                getRequiredProperty(configuration, OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_ID);
        String clientSecret = configuration
                .getSecret(OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET)
                .orElseThrow(() -> new TransconnectConnectorException("Required property missing: "
                        + OAuth2PropertiesClientCredentialsExtension.PROPERTY_CLIENT_SECRET));
        String scope = configuration
                .getString(OAuth2PropertiesClientCredentialsExtension.PROPERTY_SCOPE)
                .orElse(DEFAULT_SCOPE);

        try {
            var credential = ClientCredentialFactory.createFromSecret(clientSecret);
            clientApp = ConfidentialClientApplication.builder(clientId, credential)
                    .authority(authUrl)
                    .build();

            // Note: This implementation supports only a single scope.
            // The scope string is wrapped in a singleton set, limiting authentication to one scope per request.
            clientCredentialParameters = ClientCredentialParameters.builder(Collections.singleton(scope))
                    .build();
            silentParameters =
                    SilentParameters.builder(Collections.singleton(scope)).build();
            initialized = true;
        } catch (Exception e) {
            throw new TransconnectConnectorException("Failed to initialize", e);
        }
    }

    /**
     * Returns an access token for the configured scope. A cached token is reused if one is available,
     * otherwise a new token is acquired with the client credentials flow.
     *
     * @return a valid access token
     * @throws TransconnectConnectorException if the extension is not initialized or no token could be acquired
     */
    public String getAccessToken() throws TransconnectConnectorException {
        if (!initialized) {
            throw new TransconnectConnectorException("Extension not initialized");
        }

        try {
            // Try silent acquisition first (uses cache if available)
            return clientApp.acquireTokenSilently(silentParameters).get().accessToken();
        } catch (Exception e) {
            // On cache miss, acquire new token with client credentials
            try {
                return clientApp.acquireToken(clientCredentialParameters).get().accessToken();
            } catch (Exception ex) {
                throw new TransconnectConnectorException("Failed to acquire token", ex);
            }
        }
    }

    /**
     * Returns the underlying MSAL client application, for callers that need more than an access token.
     *
     * @return the initialized client application
     * @throws TransconnectConnectorException if the extension is not initialized
     */
    public ConfidentialClientApplication getClientApp() throws TransconnectConnectorException {
        if (!initialized) {
            throw new TransconnectConnectorException("Extension not initialized");
        }
        return clientApp;
    }

    private String getRequiredProperty(Configuration configuration, String key) throws TransconnectConnectorException {
        return configuration
                .getString(key)
                .orElseThrow(() -> new TransconnectConnectorException("Required property missing: " + key));
    }
}
