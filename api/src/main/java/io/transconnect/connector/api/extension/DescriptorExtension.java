/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.extension;

import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;

/**
 * An extension that modifies the connector description.
 *
 * <p>Implementations of this interface can be used to modify the connector description. This is useful for
 * adding additional information to the description, such as additional properties or languages.</p>
 */
public interface DescriptorExtension extends ConnectorExtension {

    /**
     * Modify the consumer connector description.
     *
     * @param description the consumer connector description
     * @return the modified consumer connector description
     */
    default ConsumerConnectorDescriptor modifyDescription(ConsumerConnectorDescriptor description) {
        return description;
    }

    /**
     * Modify the producer connector description.
     *
     * @param description the producer connector description
     * @return the modified producer connector description
     */
    default ProducerConnectorDescriptor modifyDescription(ProducerConnectorDescriptor description) {
        return description;
    }
}
