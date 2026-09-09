/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.test.reference.mocks.MockConsumerConnector;
import io.transconnect.connector.test.reference.mocks.MockProducerConnector;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class ServiceLoaderConnectorProviderTest {

    /**
     * tests if the service loader loads a MockConsumerConnector
     */
    @Test
    public void testConsumerConnectorLoader() {
        ServiceLoaderConnectorProvider provider = new ServiceLoaderConnectorProvider();

        Optional<ConsumerConnector> consumer = provider.loadConsumer(MockConsumerConnector.class);

        assertTrue(consumer.isPresent());
        assertEquals(MockConsumerConnector.class, consumer.get().getClass());
    }

    /**
     * tests if the service loader loads a MockProducerConnector
     */
    @Test
    public void testProducerConnectorLoader() {
        ServiceLoaderConnectorProvider provider = new ServiceLoaderConnectorProvider();

        Optional<ProducerConnector> producer = provider.loadProducer(MockProducerConnector.class);

        assertTrue(producer.isPresent());
        assertEquals(MockProducerConnector.class, producer.get().getClass());
    }
}
