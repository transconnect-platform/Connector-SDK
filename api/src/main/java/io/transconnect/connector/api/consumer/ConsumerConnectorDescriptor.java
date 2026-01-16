/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.consumer;

import io.transconnect.connector.api.CommonConnectorDescriptor;
import io.transconnect.connector.api.ConnectorDescriptor;
import io.transconnect.connector.api.Interaction;
import io.transconnect.connector.api.property.IConnectorProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Describes consumer connectors.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@Getter
public class ConsumerConnectorDescriptor implements ConnectorDescriptor {

    /**
     * Common connector properties.
     */
    private CommonConnectorDescriptor common;

    /**
     * Array of property specifications to configure the Connector.
     */
    @Builder.Default
    private IConnectorProperty[] properties = new IConnectorProperty[0];

    /**
     * Array of Interactions that can be executed by the Connector.
     */
    @Builder.Default
    private Interaction[] interactions = new Interaction[0];
}
