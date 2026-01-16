/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector;

import io.transconnect.connector.api.Configuration;
import java.net.URI;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A mock implementation of the {@link Configuration} interface.
 */
public final class MockConfig implements Configuration {

    /**
     * The values of the configuration.
     */
    private final Map<String, Object> values;

    /**
     * Creates a new instance of the mock configuration with the given values.
     * @param values The values of the configuration.
     */
    public MockConfig(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Creates a new instance of the mock configuration with an empty set of values.
     */
    public MockConfig() {
        this(new HashMap<>());
    }

    /**
     * Sets the value for the given key.
     * @param key The key of the value.
     * @param value The value.
     */
    public void setValue(String key, Object value) {
        values.put(key, value);
    }

    @Override
    public URI getId() {
        return null;
    }

    @Override
    public Collection<String> getKeys() {
        return values.keySet();
    }

    @Override
    public Optional<String> getString(String key) {
        return Optional.ofNullable((String) values.get(key));
    }

    @Override
    public Optional<String> getSecret(String key) {
        return Optional.ofNullable((String) values.get(key));
    }

    @Override
    public Optional<Double> getDouble(String key) {
        return Optional.ofNullable((Double) values.get(key));
    }

    @Override
    public Optional<Long> getLong(String key) {
        return Optional.ofNullable((Long) values.get(key));
    }

    @Override
    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable((Boolean) values.get(key));
    }

    @Override
    public Optional<Certificate> getCertificate(String key) {
        return Optional.ofNullable((Certificate) values.get(key));
    }

    @Override
    public Optional<PrivateKey> getPrivateKey(String key) {
        return Optional.ofNullable((PrivateKey) values.get(key));
    }
}
