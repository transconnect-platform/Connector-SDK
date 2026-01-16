/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Property to configure a connector. Before the process is started.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ConnectorProperty implements IConnectorProperty, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id of the property.
     */
    private String id;

    /**
     * localized display name of the property.
     */
    private LocalizedText[] displayName;

    /**
     * localized description of the property.
     */
    private LocalizedText[] description;

    /**
     * type of connector property.
     */
    private ConnectorPropertyType type;

    /**
     * the default value of this property.
     */
    private Serializable defaultValue;

    /**
     * flag indicating whether this property is required or not.
     */
    private boolean required;

    /**
     * validator configuration to be used when validating this property.
     */
    private ValidatorConfig validator;

    /**
     * Dependencies that this property relies on for validation.
     */
    private PropertyDependency[] dependsOn;
}
