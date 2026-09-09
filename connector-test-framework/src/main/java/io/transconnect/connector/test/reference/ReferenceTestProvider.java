/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.test.reference.marker.DefaultConsumerConnector;
import io.transconnect.connector.test.reference.marker.DefaultProducerConnector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TestCase Provider for JUnit ArgumentSource.
 * Used in custom annotation {@link ReferenceTest}.
 */
public class ReferenceTestProvider implements ArgumentsProvider, AnnotationConsumer<ReferenceTest> {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ReferenceTestProvider.class);

    /**
     * Classpath path to the folder with the reference files or to a single reference file.
     *
     * @return classpath path to the folder with the reference files or to a single reference file
     */
    @Getter
    private String xmlPath;

    /**
     * Service loader connector provider.
     *
     * @param connectorProvider service loader connector provider
     */
    @Setter
    private ConnectorProvider connectorProvider = new ServiceLoaderConnectorProvider();

    /**
     * Consumer Connector class to be loaded.
     */
    private Class<? extends ConsumerConnector> consumerConnectorClass;

    /**
     * Producer Connector class to be loaded.
     */
    private Class<? extends ProducerConnector> producerConnectorClass;

    /**
     * flag for namespace comparison.
     * true, if namespaces should be ignored, otherwise false
     */
    private boolean ignoreNamespaces = true;

    /**
     * flag for attachment comparison.
     * true, if the output attachments should be compared, otherwise false
     */
    private boolean compareAttachments = false;

    /**
     * Method to provide ReferenceTestCase objects in ArgumentsSource.
     *
     * @param extensionContext ExtensionContext to provide information through the actual test case.
     * @return Stream with Arguments
     */
    @Override
    public Stream<? extends Arguments> provideArguments(
            ParameterDeclarations params, ExtensionContext extensionContext) {

        if (xmlPath == null) {
            logger.debug("No XML Path is given");
            throw new IllegalArgumentException("No path to reference file(s).");
        }

        // a directory yields one test case per *.xml file; a single file yields exactly one test case
        if (ClasspathUtils.isDirectory(xmlPath)) {
            // work with the folder path without a trailing slash and add the separator explicitly
            String folder = xmlPath.endsWith("/") ? xmlPath.substring(0, xmlPath.length() - 1) : xmlPath;

            logger.debug("Found folder with reference files: {}", folder);
            return ClasspathUtils.listFiles(xmlPath)
                    .filter(resource ->
                            resource.getFileName().toString().toLowerCase().endsWith(".xml"))
                    .map(resource -> {
                        String resourcePath =
                                folder + "/" + resource.getFileName().toString();
                        try (InputStream xmlStream = ClasspathUtils.loadResource(resourcePath)) {
                            Path path = Paths.get(folder);
                            AbstractReferenceTestCase testCase = convertToReferenceTestCases(xmlStream, path);
                            return Arguments.of(testCase);
                        } catch (IOException | ReferenceTestException exception) {
                            logger.debug("Could not create testcase from {}", resourcePath);
                            throw new RuntimeException("Could not create testcases from " + resourcePath, exception);
                        }
                    });
        }

        // single reference file
        logger.debug("Found single reference file: {}", xmlPath);
        try (InputStream xmlStream = ClasspathUtils.loadResource(xmlPath)) {
            Path path = ClasspathUtils.convertClasspathToRegularPath(xmlPath);
            AbstractReferenceTestCase testCase = convertToReferenceTestCases(xmlStream, path);
            return Stream.of(Arguments.of(testCase));
        } catch (IOException | ReferenceTestException exception) {
            logger.debug("Could not create testcase from {}", xmlPath);
            throw new RuntimeException("Could not create testcases from " + xmlPath, exception);
        }
    }

    /**
     * Accepts the annotation and initializes the paths for XML and XSD.
     *
     * @param referenceTest The {@link ReferenceTest} annotation containing path information.
     */
    @Override
    public void accept(ReferenceTest referenceTest) {

        xmlPath = referenceTest.path();
        ignoreNamespaces = referenceTest.ignoreNamespaces();
        compareAttachments = referenceTest.compareAttachments();

        boolean hasConsumer = referenceTest.consumer() != DefaultConsumerConnector.class;
        boolean hasProducer = referenceTest.producer() != DefaultProducerConnector.class;

        if (hasProducer == hasConsumer) {
            throw new IllegalArgumentException(
                    "No connector is given in ReferenceTest annotation or you specified consumer and producer");
        }

        producerConnectorClass = referenceTest.producer();
        consumerConnectorClass = referenceTest.consumer();

        if (!ClasspathUtils.resourceExists(xmlPath)) {
            throw new IllegalArgumentException("The given reference file does not exists");
        }
    }

    /**
     * Converts a given reference XML file to ReferenceTestCases.
     *
     * @param xmlStream The content of the XML file.
     * @param xmlFile The path to the XML file.
     * @return A {@link AbstractReferenceTestCase} object depending on whether it's a producer or consumer test case
     * @throws ReferenceTestException if conversion fails.
     */
    private AbstractReferenceTestCase convertToReferenceTestCases(InputStream xmlStream, Path xmlFile)
            throws ReferenceTestException {

        Objects.requireNonNull(xmlStream, "Xml stream of the document is given");

        try {
            ReferenceParser parser = new ReferenceParser();
            // parse for setting consumer flag, if input and output is available
            // otherwise it is a producer
            parser.parse(
                    xmlStream,
                    ClasspathUtils.convertClasspathToRegularPath(xmlPath).toString());

            // build the objects
            if (parser.isConsumer()) {
                logger.debug("Creating ConsumerReferenceTestCase ...");
                ConsumerReferenceTestCase testCase =
                        new ConsumerReferenceTestCase(xmlFile.toFile(), parser, ignoreNamespaces, compareAttachments);
                testCase.loadConnector(connectorProvider, consumerConnectorClass);
                return testCase;
            } else {
                logger.debug("Creating ProducerReferenceTestCase ...");
                ProducerReferenceTestCase testCase =
                        new ProducerReferenceTestCase(xmlFile.toFile(), parser, ignoreNamespaces, compareAttachments);
                testCase.loadConnector(connectorProvider, producerConnectorClass);
                return testCase;
            }

        } catch (ReferenceTestException exception) {
            throw new ReferenceTestException("Could not convert to Reference Testcases", exception);
        }
    }
}
