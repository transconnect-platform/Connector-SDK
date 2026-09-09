/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Describes a Connector and how to interact with it.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class CommonConnectorDescriptor {

    /**
     * A URI that is a unique identifier for the connector type.
     *
     * @param type a URI that is a unique identifier for the connector type
     * @return a URI that is a unique identifier for the connector type
     */
    private URI type;

    /**
     * A list of labels that are associated with the Connector. These are free text values that help to categorize and
     * to search for connectors.
     *
     * @param labels the labels associated with the Connector
     * @return the labels associated with the Connector
     */
    private String[] labels;

    /**
     * the version of the connector as semantic version (Format: x.y.z).
     *
     * @param version the version of the connector as semantic version (Format: x.y.z)
     * @return the version of the connector as semantic version (Format: x.y.z)
     */
    private String version;

    /**
     * name of the connector vendor.
     *
     * @param vendor name of the connector vendor
     * @return name of the connector vendor
     */
    private String vendor;

    /**
     * Array of localized Connector names for displaying purposes.
     *
     * @param displayName array of localized Connector names for displaying purposes
     * @return array of localized Connector names for displaying purposes
     */
    private LocalizedText[] displayName;

    /**
     * Array of localized Connector descriptions for displaying purposes.
     *
     * @param description array of localized Connector descriptions for displaying purposes
     * @return array of localized Connector descriptions for displaying purposes
     */
    private LocalizedText[] description;
}
