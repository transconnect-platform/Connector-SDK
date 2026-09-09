/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.producer.ProducerConnector;
import java.util.Optional;

/**
 * Provider to load connectors.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ConnectorProvider {

    /**
     * Loads the consumer connector.
     *
     * @param consumerClass the consumer connector class to load
     * @return An optional containing the ConsumerConnector if it is available, or empty otherwise
     */
    Optional<ConsumerConnector> loadConsumer(Class<? extends ConsumerConnector> consumerClass);

    /**
     * Loads the producer connector.
     *
     * @param producerClass the producer connector class to load
     * @return An optional containing the ProducerConnector if it is available, or empty otherwise
     */
    Optional<ProducerConnector> loadProducer(Class<? extends ProducerConnector> producerClass);
}
