/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import java.io.Serializable;

/**
 * It represents the basic connector property interface.
 */
public interface IConnectorProperty {

    /**
     * get the id of the property.
     */
    String getId();

    /**
     * get localized display name of the property.
     */
    LocalizedText[] getDisplayName();

    /**
     * get localized description name of the property.
     */
    LocalizedText[] getDescription();

    /**
     * get type of connector property.
     */
    ConnectorPropertyType getType();

    /**
     * get the default value of this property.
     */
    Serializable getDefaultValue();

    /**
     * check whether this property is required or not.
     */
    boolean isRequired();

    /**
     * get validator configuration to be used when validating this property.
     */
    ValidatorConfig getValidator();

    /**
     * get dependencies of this property.
     */
    PropertyDependency[] getDependsOn();
}
