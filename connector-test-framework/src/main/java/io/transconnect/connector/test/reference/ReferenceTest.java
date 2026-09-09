/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.test.reference.marker.DefaultConsumerConnector;
import io.transconnect.connector.test.reference.marker.DefaultProducerConnector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.annotation.Testable;

/**
 * Reference Test annotation to extend test methods with a Provider for ArgumentSource.
 * Provides TestCases based on XML reference files.
 *
 * <h2>XML Reference File Structure</h2>
 * <p>Reference test files must follow this structure:</p>
 * <pre>{@code
 * <message xmlns="connector-namespace"
 *          xmlns:test="http://transconnect.io/connector/test"
 *          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *          xsi:schemaLocation="connector-namespace classpath:path/to/schema.xsd
 *          anotherNamespace relativePath/to/schema.xsd
 *          ">
 *
 *     &lt;test:configuration>
 *         &lt;test:property id="propertyName">value&lt;/test:property>
 *         &lt;test:property id="anotherProperty">anotherValue&lt;/test:property>
 *     &lt;/test:configuration>
 *
 *     <!-- Consumer tests only: input element with interaction URI -->
 *     &lt;input test:interaction="urn:interaction:uri">
 *         ...
 *     &lt;/input>
 *
 *     &lt;output>
 *         &lt;dynamicField test:ignore="true">ignored during comparison&lt;/dynamicField>
 *         ...
 *     &lt;/output>
 * &lt;/message>
 * }</pre>
 *
 * <h2>Test Namespace Elements and Attributes</h2>
 * <ul>
 *   <li>{@code <test:configuration>} - Container for connector configuration properties</li>
 *   <li>{@code <test:property id="...">} - Defines a connector property value.
 *       The {@code id} attribute must match the property ID from the connector descriptor.</li>
 *   <li>{@code test:interaction} - Attribute on the {@code <input>} element specifying the
 *       interaction URI (required for consumer tests)</li>
 *   <li>{@code test:ignore="true"} - Attribute to mark elements or attributes that should be
 *       ignored during result comparison (useful for timestamps, IDs, etc.)</li>
 * </ul>
 *
 * <h2>Producer vs Consumer Tests</h2>
 * <ul>
 *   <li><strong>Producer tests:</strong> Only require {@code <output>} element. The connector
 *       produces messages that are compared against the expected output.</li>
 *   <li><strong>Consumer tests:</strong> Require both {@code <input>} (with {@code test:interaction})
 *       and {@code <output>} elements. The input is sent to the connector, and the response
 *       is compared against the expected output.</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 * @see ProducerReferenceTestCase
 * @see ConsumerReferenceTestCase
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Testable
@ParameterizedTest
@ArgumentsSource(ReferenceTestProvider.class)
public @interface ReferenceTest {

    /**
     * Classpath path to the reference test data, either a directory or a single reference file.
     * <p>If the path points to a directory, every {@code *.xml} file directly inside it is turned into one
     * reference test case; all other files (e.g. attachment binaries) are ignored. A trailing slash is optional.</p>
     * <p>If the path points to a single file, exactly one reference test case is created from it.</p>
     *
     * @return classpath path to the reference file(s)
     */
    String path();

    /**
     * flag for namespace comparison.
     * @return  true, if the namespaces should be ignored, otherwise false
     */
    boolean ignoreNamespaces();

    /**
     * flag for attachment comparison.
     * <p>If enabled, every {@code <test:attachment>} declared in the {@code <output>} is compared byte for byte
     * against the attachment produced by the connector. Disabled by default.</p>
     *
     * @return true, if the output attachments should be compared, otherwise false
     */
    boolean compareAttachments() default false;

    /**
     * Producer connector class to be loaded for this test.
     * <p>Required when testing a producer connector. The XML reference file
     * should only contain {@code <output>} (no {@code <input>}).</p>
     *
     * @return the producer connector class, or {@link DefaultProducerConnector} if not specified
     */
    Class<? extends ProducerConnector> producer() default DefaultProducerConnector.class;

    /**
     * Consumer connector class to be loaded for this test.
     * <p>Required when testing a consumer connector. The XML reference file
     * must contain both {@code <input>} (with {@code test:interaction}) and {@code <output>}.</p>
     *
     * @return the consumer connector class, or {@link DefaultConsumerConnector} if not specified
     */
    Class<? extends ConsumerConnector> consumer() default DefaultConsumerConnector.class;
}
