/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property;

import io.transconnect.connector.api.LocalizedText;
import io.transconnect.connector.api.property.validator.ValidatorConfig;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class declares a connector property that can be set to on of a defined number of {@link SelectableEntry}.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class SelectConnectorProperty implements IConnectorProperty, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id of the property.
     *
     * @param id id of the property
     * @return id of the property
     */
    private String id;

    /**
     * localized display name of the property.
     *
     * @param displayName localized display name of the property
     * @return localized display name of the property
     */
    private LocalizedText[] displayName;

    /**
     * localized description of the property.
     *
     * @param description localized description of the property
     * @return localized description of the property
     */
    private LocalizedText[] description;

    /**
     * type of connector property.
     *
     * @param type type of connector property
     * @return type of connector property
     */
    private ConnectorPropertyType type;

    /**
     * the valid entries for this {@link SelectConnectorProperty}.
     *
     * @param entries the valid entries for this {@link SelectConnectorProperty}
     * @return the valid entries for this {@link SelectConnectorProperty}
     */
    private SelectableEntry[] entries;

    /**
     * the default value of this property.
     *
     * @param defaultValue the default value of this property
     * @return the default value of this property
     */
    private String defaultValue;

    /**
     * flag indicating whether this property is required or not.
     *
     * @param required flag indicating whether this property is required or not
     * @return flag indicating whether this property is required or not
     */
    private boolean required;

    /**
     * validator configuration to be used when validating this property.
     *
     * @param validator validator configuration to be used when validating this property
     * @return validator configuration to be used when validating this property
     */
    private ValidatorConfig validator;

    /**
     * Dependencies that this property relies on for validation.
     *
     * @param dependsOn dependencies that this property relies on for validation
     * @return dependencies that this property relies on for validation
     */
    private PropertyDependency[] dependsOn;

    /**
     * This class declares an entry that can be selected.
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class SelectableEntry implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * id of the entry.
         */
        private String id;

        /**
         * localized display name of the entry.
         */
        private LocalizedText[] displayName;
    }
}
