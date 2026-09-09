/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.property.IConnectorProperty;
import io.transconnect.connector.test.reference.mocks.MockConsumerConnector;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConsumerReferenceTestCaseTest {

    ConsumerReferenceTestCase testableTestCase;

    Map<String, String> referenceConfiguration;

    ReferenceParser mockParser;

    @BeforeEach
    public void before() {

        referenceConfiguration = new HashMap<>();

        mockParser = mock(ReferenceParser.class);
        when(mockParser.getMessage(true)).thenReturn("");
        when(mockParser.getMessage(false)).thenReturn("");
        when(mockParser.isConsumer()).thenReturn(true);
        when(mockParser.getConfiguration()).thenReturn(referenceConfiguration);

        ConsumerReferenceTestCase consumerTestCase =
                new ConsumerReferenceTestCase(new File("reference.xml"), mockParser);
        testableTestCase = spy(consumerTestCase);
    }

    /**
     * Tests the loading of a consumer connector (MockConsumerConnector)
     */
    @Test
    public void testLoadConnector() {

        // call the method
        assertDoesNotThrow(() ->
                testableTestCase.loadConnector(new ServiceLoaderConnectorProvider(), MockConsumerConnector.class));
    }

    /**
     * This test the loading of a consumer connector with no connector is loaded
     */
    @Test
    public void testLoadConnectorNoConnector() {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        when(providerMock.loadConsumer(any())).thenReturn(Optional.empty());

        // call the method
        assertThrows(
                ReferenceTestException.class,
                () -> testableTestCase.loadConnector(providerMock, MockConsumerConnector.class));
    }

    /**
     * This test checks the configuration creation with a loaded connector.
     */
    @Test
    public void testLoadConnectorWithoutConfigMap() {

        ReferenceParser parserMock = mock(ReferenceParser.class);
        when(parserMock.getConfiguration()).thenReturn(null);
        when(parserMock.getMessage(true)).thenReturn("");
        when(parserMock.getMessage(false)).thenReturn("");

        ConsumerReferenceTestCase testCase = new ConsumerReferenceTestCase(new File("reference.xml"), parserMock);

        // load MockConsumerConnector with reference configuration map
        assertThrows(
                ReferenceTestException.class,
                () -> testCase.loadConnector(new ServiceLoaderConnectorProvider(), MockConsumerConnector.class));
    }

    /**
     * This test checks the configuration creation with no loaded connector.
     */
    @Test
    public void testRunWithoutConnector() {
        assertThrows(ReferenceTestException.class, () -> testableTestCase.run());
    }

    /**
     * Tests the run method without a configuration
     */
    @Test
    public void testRunWithoutConfiguration() {
        ReferenceParser parserMock = mock(ReferenceParser.class);
        when(parserMock.getConfiguration()).thenReturn(null);
        when(parserMock.getMessage(true)).thenReturn("");
        when(parserMock.getMessage(false)).thenReturn("");
        ConsumerReferenceTestCase testCase = new ConsumerReferenceTestCase(new File("reference.xml"), parserMock);

        // load MockConsumerConnector with reference configuration map
        assertThrows(
                ReferenceTestException.class,
                () -> testCase.loadConnector(new ServiceLoaderConnectorProvider(), MockConsumerConnector.class));

        // call the run method
        assertThrows(ReferenceTestException.class, testCase::run);
    }

    /**
     * Tests the run method with connector and configuration available.
     * @throws Exception if the test fails
     */
    @Test
    public void testRunWithConnectorAndConfiguration() throws Exception {
        // stub compare differences
        doReturn(null).when(testableTestCase).compareResults(any());

        // load the connector
        testableTestCase.loadConnector(new ServiceLoaderConnectorProvider(), MockConsumerConnector.class);
        assertNotNull(testableTestCase.run());
    }

    /**
     * tests the run method with disconnected connection
     */
    @Test
    public void testRunWithConnectorConnectionIsNotConnected() {
        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        ConsumerConnector connectorMock = mock(ConsumerConnector.class);
        ConsumerConnection connectionMock = mock(ConsumerConnection.class);
        ConsumerConnectorDescriptor descriptorMock = mock(ConsumerConnectorDescriptor.class);
        IConnectorProperty[] properties = {};

        // mock behavior
        when(providerMock.loadConsumer(any())).thenReturn(Optional.of(connectorMock));
        when(connectorMock.getDescription()).thenReturn(descriptorMock);
        when(descriptorMock.getProperties()).thenReturn(properties);
        when(connectorMock.createConnection(any())).thenReturn(connectionMock);
        when(connectionMock.isConnected()).thenReturn(false);

        // load the connector mock
        assertDoesNotThrow(() -> testableTestCase.loadConnector(providerMock, MockConsumerConnector.class));

        // run the method
        AtomicReference<Message> resultMessage = new AtomicReference<>();
        assertDoesNotThrow(() -> resultMessage.set(testableTestCase.run()));
        assertNotNull(resultMessage.get());
    }

    /**
     * Tests that a test:attachment declared in the input is attached to the input message.
     * @throws Exception if the test fails
     */
    @Test
    public void testInputMessageContainsAttachment() throws Exception {

        String resource = "io/transconnect/connector/test/reference/ReferenceTestInputAttachment/consumer-attachment.xml";
        File refFile = ClasspathUtils.convertClasspathToRegularPath(resource).toFile();

        ReferenceParser parser = new ReferenceParser();
        try (InputStream in = ClasspathUtils.loadResource(resource)) {
            parser.parse(in, refFile.toString());
        }

        ConsumerReferenceTestCase testCase = new ConsumerReferenceTestCase(refFile, parser);

        Message input = testCase.getInput();
        assertTrue(input.getAttachmentIds().contains("additionalText"));
        try (InputStream attachment = input.getAttachment("additionalText")) {
            assertArrayEquals("extra content".getBytes(StandardCharsets.UTF_8), attachment.readAllBytes());
        }
    }
}
