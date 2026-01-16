/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

/**
 * Validates a given value.
 */
@FunctionalInterface
public interface Validator {

    /**
     * Checks whether the passed value is valid or not.
     *
     * @param value the value to validate
     * @return true if the value is valid, otherwise false
     */
    boolean isValid(Object value);
}
