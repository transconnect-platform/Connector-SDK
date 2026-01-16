/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the configuration for property dependencies.
 * A dependency consists of an operation, a value, and an array of constraints.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PropertyDependency implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The operation defining the dependency logic.
     */
    private ConnectorPropertyConstraintOperation operation;

    /**
     * The value associated with the dependency.
     */
    private Serializable value;

    /**
     * An array of constraints applicable to this dependency.
     */
    private PropertyConstraint[] constraints;
}
