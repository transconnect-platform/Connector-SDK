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
 * A container class for managing properties and their dependencies.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class PropertyValueContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * An array of all properties within the current context.
     */
    private IConnectorProperty[] properties;

    /**
     * The property currently being operated on or in focus.
     */
    private IConnectorProperty currentProperty;
}
