/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

/**
 * Enum representing the types of operations that can be applied to property constraints.
 */
public enum ConnectorPropertyConstraintOperation {
    // Operations for equality checks
    EQUALS,
    NOT_EQUALS,

    // Operations for numeric comparisons
    GREATER,
    GREATER_OR_EQUALS,
    LOWER,
    LOWER_OR_EQUALS,

    // Operations for null checks
    NULL,
    NOT_NULL,

    // Operations for string-based comparisons
    EMPTY,
    NOT_EMPTY,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    MATCHES,
}
