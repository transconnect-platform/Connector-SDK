/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck;

import io.transconnect.connector.api.ConnectorDescriptor;
import io.transconnect.connector.api.IConnector;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;

/**
 * Base class for the TCK checks on the connector properties declared by a {@link ConnectorDescriptor}.
 *
 * <p>The checks are implemented as plain methods so that a concrete subclass can expose them as
 * parameterized JUnit tests fed by a connector source. See
 * {@link io.transconnect.connector.tck.consumer.ConsumerPropertyTest} and
 * {@link io.transconnect.connector.tck.producer.ProducerPropertyTest}.
 */
public abstract class AbstractPropertyTest {

    /**
     * Asserts that no property ID is declared twice by the given connector.
     *
     * @param connector the connector under test
     */
    protected void testPropertyIdUniqueness(IConnector<? extends ConnectorDescriptor> connector) {
        ConnectorDescriptor descriptor = connector.getDescription();
        Set<String> propertyIds = new HashSet<>();
        for (IConnectorProperty property : descriptor.getProperties()) {
            Assertions.assertFalse(
                    propertyIds.contains(property.getId()),
                    "Duplicate property ID '" + property.getId() + "' found in Connector "
                            + connector.getClass().getName());
            propertyIds.add(property.getId());
        }
    }

    /**
     * Asserts that no property ID starts with {@code $}. That prefix is reserved for properties
     * injected by the platform and by extensions.
     *
     * @param connector the connector under test
     */
    protected void testPropertyIdStartsNotWithDollar(IConnector<? extends ConnectorDescriptor> connector) {
        ConnectorDescriptor descriptor = connector.getDescription();
        for (IConnectorProperty property : descriptor.getProperties()) {
            Assertions.assertFalse(
                    property.getId().startsWith("$"),
                    "Property ID '" + property.getId() + "' starts with '$' in Connector "
                            + connector.getClass().getName() + ". This is a reserved prefix and is not allowed.");
        }
    }

    /**
     * Asserts that the default value of every property is an instance of the class declared by the
     * property type.
     *
     * @param connector the connector under test
     */
    protected void testPropertyDefaultValueType(IConnector<? extends ConnectorDescriptor> connector) {
        ConnectorDescriptor descriptor = connector.getDescription();
        for (IConnectorProperty property : descriptor.getProperties()) {
            Object defaultValue = property.getDefaultValue();
            if (defaultValue != null) {
                Class<?> propertyType = property.getType().getClassId();
                if (!propertyType.isInstance(defaultValue)) {
                    throw new AssertionError("Default value type mismatch for property '" + property.getId()
                            + "': expected " + propertyType.getName() + " but got "
                            + defaultValue.getClass().getName() + " in Connector "
                            + connector.getClass().getName());
                }
            }
        }
    }
}
