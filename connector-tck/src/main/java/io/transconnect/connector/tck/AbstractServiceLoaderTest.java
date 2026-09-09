/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck;

import io.transconnect.connector.api.extension.Connector;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Base class for the TCK checks on the {@link ServiceLoader} registration of a connector.
 *
 * <p>It verifies that the connector is discoverable through {@code META-INF/services} and that every
 * discovered implementation carries the {@link Connector} annotation. Subclasses only have to name the
 * connector interface to look up.
 */
public abstract class AbstractServiceLoaderTest {

    /**
     * Returns the connector interface the {@link ServiceLoader} is asked for.
     *
     * @return the connector interface under test, e.g.
     * {@code io.transconnect.connector.api.consumer.ConsumerConnector}
     */
    protected abstract Class<?> getConnectorClass();

    @Test
    void testLoadConnectors() {
        Class<?> clazz = getConnectorClass();
        var connectors = ServiceLoader.load(clazz);
        Assertions.assertTrue(
                connectors.findFirst().isPresent(),
                "No " + clazz.getSimpleName() + " implementations found via ServiceLoader. Check that at"
                        + " least one connector is present and that the service files are correctly configured"
                        + " (Classpath file 'META-INF/services/" + clazz.getName() + "').");
    }

    @Test
    void testConnectorAnnotation() {
        Class<?> clazz = getConnectorClass();
        var connectors = ServiceLoader.load(clazz);
        connectors.forEach(c -> Assertions.assertNotNull(
                c.getClass().getAnnotation(Connector.class),
                clazz.getSimpleName() + " implementation " + c.getClass().getName()
                        + " is missing the @Connector annotation."));
    }
}
