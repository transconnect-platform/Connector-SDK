/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.producer.ProducerConnector;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * ConnectorProvider to load consumer and producer with ServiceLoader.
 *
 * @version 1.0
 * @since 1.0
 */
public class ServiceLoaderConnectorProvider implements ConnectorProvider {

    /**
     * Loads a consumer connector using the ServiceLoader.
     *
     * @return Optional containing the ConsumerConnector if found, or empty otherwise
     */
    @Override
    public Optional<ConsumerConnector> loadConsumer(Class<? extends ConsumerConnector> connectorClass) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return StreamSupport.stream(
                        ServiceLoader.load(ConsumerConnector.class, contextClassLoader)
                                .spliterator(),
                        false)
                .filter(connectorClass::isInstance)
                .findFirst();
    }

    /**
     * Loads a producer connector using the ServiceLoader.
     *
     * @return Optional containing the ProducerConnector if found, or empty otherwise
     */
    @Override
    public Optional<ProducerConnector> loadProducer(Class<? extends ProducerConnector> connectorClass) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return StreamSupport.stream(
                        ServiceLoader.load(ProducerConnector.class, contextClassLoader)
                                .spliterator(),
                        false)
                .filter(connectorClass::isInstance)
                .findFirst();
    }
}
