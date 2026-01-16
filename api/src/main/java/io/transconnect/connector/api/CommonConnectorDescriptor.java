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
     */
    private URI type;

    /**
     * A list of labels that are associated with the Connector. These are free fields that are helpful for the
     */
    private String[] labels;

    /**
     * the version of the connector as semantic version (Format: x.y.z).
     */
    private String version;

    /**
     * name of the connector vendor.
     */
    private String vendor;

    /**
     * Array of localized Connector names for displaying purposes.
     */
    private LocalizedText[] displayName;

    /**
     * Array of localized Connector descriptions for displaying purposes.
     */
    private LocalizedText[] description;
}
