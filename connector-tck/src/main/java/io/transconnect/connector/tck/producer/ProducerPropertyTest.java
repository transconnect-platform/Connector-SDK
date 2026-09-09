/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck.producer;

import io.transconnect.connector.api.ConnectorDescriptor;
import io.transconnect.connector.api.IConnector;
import io.transconnect.connector.tck.AbstractPropertyTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Runs the TCK property checks against every producer connector registered via the
 * {@link java.util.ServiceLoader}. Extend this class in your connector project to have the checks
 * executed as part of your test suite.
 */
public class ProducerPropertyTest extends AbstractPropertyTest {

    /**
     * {@inheritDoc}
     */
    @ParameterizedTest
    @ArgumentsSource(ProducerArgumentsProvider.class)
    @Override
    protected void testPropertyDefaultValueType(IConnector<? extends ConnectorDescriptor> connector) {
        super.testPropertyDefaultValueType(connector);
    }

    /**
     * {@inheritDoc}
     */
    @ParameterizedTest
    @ArgumentsSource(ProducerArgumentsProvider.class)
    @Override
    protected void testPropertyIdStartsNotWithDollar(IConnector<? extends ConnectorDescriptor> connector) {
        super.testPropertyIdStartsNotWithDollar(connector);
    }

    /**
     * {@inheritDoc}
     */
    @ParameterizedTest
    @ArgumentsSource(ProducerArgumentsProvider.class)
    @Override
    protected void testPropertyIdUniqueness(IConnector<? extends ConnectorDescriptor> connector) {
        super.testPropertyIdUniqueness(connector);
    }
}
