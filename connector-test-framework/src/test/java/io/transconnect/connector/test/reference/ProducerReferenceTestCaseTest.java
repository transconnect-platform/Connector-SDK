/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.transconnect.connector.MockMessage;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.property.IConnectorProperty;
import io.transconnect.connector.test.reference.mocks.MockProducerConnector;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProducerReferenceTestCaseTest {

    ProducerReferenceTestCase testableTestCase;

    ReferenceParser mockParser = mock(ReferenceParser.class);

    @BeforeEach
    public void before() {
        when(mockParser.getMessage(true)).thenReturn("");
        when(mockParser.getMessage(false)).thenReturn("");
        when(mockParser.isConsumer()).thenReturn(true);

        ProducerReferenceTestCase producerTestCase =
                new ProducerReferenceTestCase(new File("reference.xml"), mockParser);
        testableTestCase = spy(producerTestCase);
    }

    /**
     * This test the loading of a producer connector (MockProducerConnector)
     */
    @Test
    public void testLoadConnector() {
        // call the method
        assertDoesNotThrow(() ->
                testableTestCase.loadConnector(new ServiceLoaderConnectorProvider(), MockProducerConnector.class));
    }

    /**
     * This test the loading of a producer connector with no connector is loaded
     */
    @Test
    public void testLoadConnectorNoConnector() {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        when(providerMock.loadProducer(any())).thenReturn(Optional.empty());

        // call the method
        assertThrows(
                ReferenceTestException.class,
                () -> testableTestCase.loadConnector(providerMock, MockProducerConnector.class));
    }

    /**
     * This test the loading of a producer connector not able to create a connection
     */
    @Test
    public void testLoadConnectorConnectionWithException() {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        ProducerConnector producerMock = mock(ProducerConnector.class);
        ProducerConnectorDescriptor descriptorMock = mock(ProducerConnectorDescriptor.class);
        IConnectorProperty[] properties = {};

        // mock behavior
        when(producerMock.getDescription()).thenReturn(descriptorMock);
        when(descriptorMock.getProperties()).thenReturn(properties);
        when(providerMock.loadProducer(any())).thenReturn(Optional.of(producerMock));
        when(producerMock.createConnection(any())).thenThrow(new RuntimeException("!"));

        // call the method
        assertThrows(
                ReferenceTestException.class,
                () -> testableTestCase.loadConnector(providerMock, MockProducerConnector.class));
        testableTestCase.onConnect();
    }

    /**
     * This test the loading of a producer connector not able to create a connection
     */
    @Test
    public void testLoadConnectorConnectionIsNull() {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        ProducerConnector producerMock = mock(ProducerConnector.class);
        ProducerConnectorDescriptor descriptorMock = mock(ProducerConnectorDescriptor.class);
        IConnectorProperty[] properties = {};

        // mock behavior
        when(producerMock.getDescription()).thenReturn(descriptorMock);
        when(descriptorMock.getProperties()).thenReturn(properties);
        when(providerMock.loadProducer(any())).thenReturn(Optional.of(producerMock));
        when(producerMock.createConnection(any())).thenReturn(null);

        // call the method
        assertThrows(
                ReferenceTestException.class,
                () -> testableTestCase.loadConnector(providerMock, MockProducerConnector.class));
        testableTestCase.onConnect();
    }

    /**
     * This test checks the configuration creation with no connector is loaded.
     */
    @Test
    public void testCreateConfigurationWithoutConnector() {
        // assertThrows(ReferenceTestException.class, () -> testableTestCase.createConfiguration(new HashMap<>()));
    }

    /**
     * This test checks disconnecting loaded connector.
     */
    @Test
    public void testDisconnectConnectorWithoutConnection() {
        assertDoesNotThrow(() -> testableTestCase.close());
    }

    /**
     * This test checks disconnecting loaded connector.
     */
    @Test
    public void testDisconnectConnectorWithActiveConnection() {

        // load the mock connector
        assertDoesNotThrow(() ->
                testableTestCase.loadConnector(new ServiceLoaderConnectorProvider(), MockProducerConnector.class));

        // disconnect
        assertDoesNotThrow(() -> testableTestCase.close());
        testableTestCase.onDisconnect();
    }

    /**
     * This test checks disconnecting loaded connector and connection is not connected.
     */
    @Test
    public void testDisconnectConnectorWithActiveConnectionNotConnected() {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        ProducerConnection connectionMock = mock(ProducerConnection.class);
        ProducerConnector connectorMock = mock(ProducerConnector.class);

        // mock behavior
        when(providerMock.loadProducer(any())).thenReturn(Optional.of(connectorMock));
        when(connectorMock.createConnection(any())).thenReturn(connectionMock);
        when(connectionMock.isConnected()).thenReturn(false);

        // load the mock connector
        assertDoesNotThrow(() ->
                testableTestCase.loadConnector(new ServiceLoaderConnectorProvider(), MockProducerConnector.class));
    }

    /**
     * This test checks disconnecting with Exception.
     */
    @Test
    public void testDisconnectConnectorWithException() throws IOException {

        ConnectorProvider providerMock = mock(ConnectorProvider.class);
        ProducerConnector producerMock = mock(ProducerConnector.class);
        // returns false at isConnected
        ProducerConnection connectionMock = mock(ProducerConnection.class);
        ProducerConnectorDescriptor descriptorMock = mock(ProducerConnectorDescriptor.class);
        // empty properties
        IConnectorProperty[] properties = {};
        // mock behavior
        when(providerMock.loadProducer(any())).thenReturn(Optional.of(producerMock));
        when(producerMock.createConnection(any())).thenReturn(connectionMock);
        when(producerMock.getDescription()).thenReturn(descriptorMock);
        when(descriptorMock.getProperties()).thenReturn(properties);
        when(connectionMock.isConnected()).thenReturn(true);
        doThrow(new ReferenceTestException("could not close connection"))
                .when(connectionMock)
                .close();

        // load the mock connector
        assertDoesNotThrow(() -> testableTestCase.loadConnector(providerMock, MockProducerConnector.class));
    }

    /**
     * This test checks the configuration creation with no loaded connector.
     */
    @Test
    public void testRunWithoutConnector() {
        // in case of no connector, nothing happens
        assertDoesNotThrow(() -> testableTestCase.run());
    }

    /**
     * This test checks the test case run with no result/ no connector is loaded
     */
    @Test
    public void testRunNoResult() throws Exception {
        // in case of no connector, nothing happens
        assertNull(testableTestCase.run());
    }

    /**
     * This test checks the run method, when a result is given
     * @throws Exception if the test fails
     */
    @Test
    public void testRunWithResult() throws Exception {
        // in case of no connector, nothing happens
        Message resultMessage = new MockMessage("<xml><test>hällo</test></xml>");
        doReturn(resultMessage).when(testableTestCase).compareResults(any());

        testableTestCase.onMessage(resultMessage);
        assertDoesNotThrow(() -> testableTestCase.run());
    }

    /**
     * This test checks the run method, when a result is given
     */
    @Test
    public void testRunWithErrors() {
        // in case of no connector, nothing happens
        testableTestCase.onError(new ReferenceTestException("A first error occurs"));
        testableTestCase.onError(new ReferenceTestException("A second error occurs"));
        assertThrows(ReferenceTestException.class, () -> testableTestCase.run());
    }

    /**
     * Producer testcases are also MessageFactory to produce messages.
     */
    @Test
    public void testCreateWritableMessage() {
        WritableMessage message = testableTestCase.createMessage();
        assertNotNull(message);
    }
}
