/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import java.net.URI;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Optional;

/**
 * The connector configuration that is passed to a new connection. The configuration entries are provided by the user.
 * The keys and values are described in the configuration schema. Defined in the {@link CommonConnectorDescriptor}.
 */
public interface Configuration {

    /**
     * The ID of this configuration. It is unique for each connection instance.
     *
     * @return the ID of the configuration
     */
    URI getId();

    /**
     * A collection of configuration keys which can be used to get the value with a method that matches the value type.
     *
     * @return a collection of keys used in the configuration
     */
    Collection<String> getKeys();

    /**
     * Returns the value of this configuration property if it is a String. If the value is no String or not set the
     * returned Optional is empty.
     *
     * @param key the key of the property
     * @return the value if it is a String, otherwise an empty Optional
     */
    Optional<String> getString(String key);

    /**
     * Returns the value of this configuration property if it is a Secret. If the value is no Secret or not set the
     * returned Optional is empty.
     *
     * @param key the key of the property
     * @return the value if it is a Secret, otherwise an empty Optional
     */
    Optional<String> getSecret(String key);

    /**
     * Returns the value of this configuration property if it is a Double. If the value is no Double or not set the
     * returned Optional is empty.
     *
     * @param key the key of the property
     * @return the value if it is a Double, otherwise an empty Optional
     */
    Optional<Double> getDouble(String key);

    /**
     * Returns the value of this configuration property if it is a Long. If the value is no Long or not set the returned
     * Optional is empty.
     *
     * @param key the key of the property
     * @return the value if it is a Long, otherwise an empty Optional
     */
    Optional<Long> getLong(String key);

    /**
     * Returns the value of this configuration property if it is a Boolean. If the value is no Boolean or not set the
     * returned Optional is empty.
     *
     * @param key the key of the property
     * @return the value if it is a Boolean, otherwise an empty Optional
     */
    Optional<Boolean> getBoolean(String key);

    /**
     * Retrieves the value of the specified configuration property as a {@link Certificate}, if available.
     *
     * <p>If the value is not set or is not an instance of {@code Certificate}, an empty {@link Optional} is returned.</p>
     *
     * @param key the key identifying the configuration property
     * @return an {@code Optional} containing the {@code Certificate} if present and of the correct type;
     *         otherwise, an empty {@code Optional}
     */
    Optional<Certificate> getCertificate(String key);

    /**
     * Retrieves the value of the specified configuration property as a {@link PrivateKey}, if available.
     *
     * <p>If the value is not set or is not an instance of {@code PrivateKey}, an empty {@link Optional} is returned.</p>
     *
     * @param key the key identifying the configuration property
     * @return an {@code Optional} containing the {@code PrivateKey} if present and of the correct type;
     *         otherwise, an empty {@code Optional}
     */
    Optional<PrivateKey> getPrivateKey(String key);
}
