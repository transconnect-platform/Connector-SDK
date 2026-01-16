/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 * Types of Property values.
 */
public enum ConnectorPropertyType {

    /**
     * The property describes a text.
     */
    TEXT(String.class),
    /**
     * The property describes a secret text like a password.
     */
    SECRET(String.class),
    /**
     * The property describes an integer value.
     */
    INTEGER(Integer.class),
    /**
     * The property describes a floating point number.
     */
    FLOAT(Double.class),
    /**
     * The property describes a boolean value.
     */
    BOOLEAN(Boolean.class),
    /**
     * The property value is selectable from given values.
     */
    SELECT(String.class),
    /**
     * The property represents a security certificate.
     */
    CERTIFICATE(Certificate.class),
    /**
     * The property represents a private key.
     */
    PRIVATE_KEY(PrivateKey.class);
    /**
     * the class that is used to implement the given type.
     */
    private final Class<?> classId;

    ConnectorPropertyType(Class<?> classId) {
        this.classId = classId;
    }

    /**
     * Returns the class that is associated with this connector property type.
     * @return the class
     */
    public Class<?> getClassId() {
        return classId;
    }
}
