/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

/**
 * Enum representing the types of operations that can be applied to property constraints.
 */
public enum ConnectorPropertyConstraintOperation {

    // Operations for equality checks

    /**
     * The value is equal to the compared value.
     */
    EQUALS,
    /**
     * The value is not equal to the compared value.
     */
    NOT_EQUALS,

    // Operations for numeric comparisons

    /**
     * The value is greater than the compared value.
     */
    GREATER,
    /**
     * The value is greater than or equal to the compared value.
     */
    GREATER_OR_EQUALS,
    /**
     * The value is lower than the compared value.
     */
    LOWER,
    /**
     * The value is lower than or equal to the compared value.
     */
    LOWER_OR_EQUALS,

    // Operations for null checks

    /**
     * The value is not set.
     */
    NULL,
    /**
     * The value is set.
     */
    NOT_NULL,

    // Operations for string-based comparisons

    /**
     * The value is an empty string.
     */
    EMPTY,
    /**
     * The value is a non-empty string.
     */
    NOT_EMPTY,
    /**
     * The value contains the compared value.
     */
    CONTAINS,
    /**
     * The value starts with the compared value.
     */
    STARTS_WITH,
    /**
     * The value ends with the compared value.
     */
    ENDS_WITH,
    /**
     * The value matches the compared value as a regular expression.
     */
    MATCHES,
}
