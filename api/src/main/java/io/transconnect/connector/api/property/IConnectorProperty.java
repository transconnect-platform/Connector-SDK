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
     *
     * @return the id of the property
     */
    String getId();

    /**
     * get localized display name of the property.
     *
     * @return the localized display names of the property
     */
    LocalizedText[] getDisplayName();

    /**
     * get localized description of the property.
     *
     * @return the localized descriptions of the property
     */
    LocalizedText[] getDescription();

    /**
     * get type of connector property.
     *
     * @return the type of the connector property
     */
    ConnectorPropertyType getType();

    /**
     * get the default value of this property.
     *
     * @return the default value, or {@code null} if the property has none
     */
    Serializable getDefaultValue();

    /**
     * check whether this property is required or not.
     *
     * @return true if the property must be set, otherwise false
     */
    boolean isRequired();

    /**
     * get validator configuration to be used when validating this property.
     *
     * @return the validator configuration, or {@code null} if the property is not validated
     */
    ValidatorConfig getValidator();

    /**
     * get dependencies of this property.
     *
     * @return the properties this property depends on, or {@code null} if it depends on none
     */
    PropertyDependency[] getDependsOn();
}
