/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.producer;

import io.transconnect.connector.api.CommonConnectorDescriptor;
import io.transconnect.connector.api.ConnectorDescriptor;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Describes producer connectors.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
@Getter
public class ProducerConnectorDescriptor implements ConnectorDescriptor {

    /**
     * Common connector properties, like name and version.
     */
    private CommonConnectorDescriptor common;

    /**
     * Array of property specifications to configure the Connector.
     */
    @Builder.Default
    private IConnectorProperty[] properties = new IConnectorProperty[0];

    /**
     * The URI that can be used to load the XSD resource from the classpath that describes the produced message. The
     * produced message is the message that the producer connector produces.
     */
    private URI producedMessageXsd;

    /**
     * The URI that can be used to load the XSD resource from the classpath that describes the result message. The
     * result message is the message that the producer connector expects get back from TRANSCONNECT for every message
     * that it had produced. Can be null if no result message is expected or if the connector works synchronously only.
     */
    private URI resultMessageXsd;
}
