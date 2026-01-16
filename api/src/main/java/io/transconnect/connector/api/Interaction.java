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
 * An interaction describes an operation that is executable with this connector.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Interaction {

    /**
     * the Interaction ID as URI. This should specify a single interaction.
     */
    private URI id;

    /**
     * Array of localized Interaction names for displaying purposes.
     */
    private LocalizedText[] displayName;

    /**
     * Array of localized Interaction descriptions for displaying purposes.
     */
    private LocalizedText[] description;

    /**
     * The URI that can be used to locate and load a XSD resource to describe the schema of the Message body that is
     * handled by the interaction.
     */
    private URI inMessageXsd;

    /**
     * The URI that can be used to locate and load a XSD resource to describe the schema of the Message body that this
     * interaction produces.
     */
    private URI outMessageXsd;
}
