/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.tck.producer;

import io.transconnect.connector.api.producer.ProducerConnector;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

/**
 * Supplies every {@link ProducerConnector} found on the classpath through the {@link ServiceLoader} as
 * an argument to a parameterized test.
 */
public class ProducerArgumentsProvider implements ArgumentsProvider {

    /**
     * {@inheritDoc}
     *
     * @return one argument per {@link ProducerConnector} registered via the {@link ServiceLoader}
     */
    @Override
    public Stream<? extends Arguments> provideArguments(
            ParameterDeclarations params, ExtensionContext extensionContext) {
        var connectors = ServiceLoader.load(ProducerConnector.class);
        return connectors.stream().map(ServiceLoader.Provider::get).map(Arguments::of);
    }
}
