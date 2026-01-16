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
 * Represents a single constraint on a property.
 * Each constraint specifies the property ID, the operation to be applied, and the target value.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PropertyConstraint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the property to which the constraint applies.
     */
    private String propertyId;

    /**
     * The operation defining the constraint logic.
     */
    private ConnectorPropertyConstraintOperation operation;

    /**
     * The target value for the constraint.
     */
    private Serializable value;
}
