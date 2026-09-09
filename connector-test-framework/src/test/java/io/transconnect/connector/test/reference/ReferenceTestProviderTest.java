/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import io.transconnect.connector.test.reference.marker.DefaultConsumerConnector;
import io.transconnect.connector.test.reference.marker.DefaultProducerConnector;
import io.transconnect.connector.test.reference.mocks.MockConsumerConnector;
import io.transconnect.connector.test.reference.mocks.MockProducerConnector;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

public class ReferenceTestProviderTest {

    private ReferenceTestProvider provider;
    private ReferenceTest dirReferenceTestMock;
    private ReferenceTest nullReferenceTestMock;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        provider = spy(new ReferenceTestProvider());

        // create default mock of ReferenceTest, returns a directory path
        dirReferenceTestMock = mock(ReferenceTest.class);
        when(dirReferenceTestMock.path()).thenReturn("io/transconnect/connector/test/reference/ReferenceTestConsumer/");
        when(dirReferenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(dirReferenceTestMock.producer()).thenAnswer(invocation -> DefaultProducerConnector.class);

        // create file mock without path
        nullReferenceTestMock = mock(ReferenceTest.class);
        when(nullReferenceTestMock.path()).thenReturn(null);
        when(nullReferenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(nullReferenceTestMock.producer()).thenAnswer(invocation -> DefaultProducerConnector.class);
    }

    /**
     * This Test checks the accept method with given xsd and path to the reference file.
     */
    @Test
    public void testAccept() {
        provider.accept(dirReferenceTestMock);
        assertEquals("io/transconnect/connector/test/reference/ReferenceTestConsumer/", provider.getXmlPath());
    }

    /**
     * This test checks the behavior of the accept method when the required XML file is missing.
     */
    @Test
    public void testAcceptMissingFolder() {
        provider = spy(new ReferenceTestProvider()); // new instance

        try (MockedStatic<ClasspathUtils> utils = mockStatic(ClasspathUtils.class)) {

            utils.when(() -> ClasspathUtils.resourceExists(anyString())).thenReturn(false);
            utils.when(() -> ClasspathUtils.resourceExists(
                            "io/transconnect/connector/test/reference/xsd/test-reference.xsd"))
                    .thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> provider.accept(dirReferenceTestMock));
        }
    }

    /**
     * test to check for no connector is given in ReferenceTest Annotation.
     */
    @Test
    public void testEqualConnectorClasses() {
        ReferenceTest referenceTestMock = mock(ReferenceTest.class);
        when(referenceTestMock.path()).thenReturn("io/transconnect/connector/test/reference/ReferenceTestProvider/");
        when(referenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(referenceTestMock.producer()).thenAnswer(invocation -> MockProducerConnector.class);
        assertThrows(IllegalArgumentException.class, () -> provider.accept(referenceTestMock));
    }

    /**
     * test for adding producer and consumer in the annotation of ReferenceTest
     */
    @Test
    public void testNoUnspecifiedConnector() {
        ReferenceTest referenceTestMock = mock(ReferenceTest.class);
        when(referenceTestMock.path()).thenReturn("io/transconnect/connector/test/reference/ReferenceTestProvider/");
        when(referenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(referenceTestMock.producer()).thenAnswer(invocation -> MockProducerConnector.class);
        assertThrows(IllegalArgumentException.class, () -> provider.accept(referenceTestMock));
    }

    /**
     * This test checks the behavior of the provideArguments method when no XML files are available to process.
     */
    @Test
    public void testProvideArgumentsWithoutXml() {
        try (MockedStatic<ClasspathUtils> utils = mockStatic(ClasspathUtils.class)) {
            utils.when(() -> ClasspathUtils.resourceExists(any())).thenReturn(true);
            provider.accept(nullReferenceTestMock);
            assertThrows(
                    IllegalArgumentException.class,
                    () -> provider.provideArguments(mock(ParameterDeclarations.class), mock(ExtensionContext.class)));
        }
    }

    @Test
    public void testProvideArgumentsWithConsumerFolder() {
        String path = "io/transconnect/connector/test/reference/ReferenceTestConsumer";

        // Mock the Context
        ExtensionContext contextMock = mock(ExtensionContext.class);

        // prepare the provider
        ReferenceTest referenceTestMock = mock(ReferenceTest.class);
        ParameterDeclarations mockDeclaration = mock(ParameterDeclarations.class);
        when(referenceTestMock.path()).thenReturn(path);
        when(referenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(referenceTestMock.producer()).thenAnswer(invocation -> DefaultProducerConnector.class);
        provider.accept(referenceTestMock);

        Stream<? extends Arguments> arguments =
                assertDoesNotThrow(() -> provider.provideArguments(mockDeclaration, contextMock));
        assertEquals(2, arguments.count());
    }

    @Test
    public void testProvideArgumentsWithProducerFolder() {
        String path = "io/transconnect/connector/test/reference/ReferenceTestProducer/";

        // Mock the Context
        ExtensionContext contextMock = mock(ExtensionContext.class);

        // prepare the provider
        ReferenceTest referenceTestMock = mock(ReferenceTest.class);
        ParameterDeclarations mockDeclaration = mock(ParameterDeclarations.class);
        when(referenceTestMock.path()).thenReturn(path);
        when(referenceTestMock.consumer()).thenAnswer(invocation -> DefaultConsumerConnector.class);
        when(referenceTestMock.producer()).thenAnswer(invocation -> MockProducerConnector.class);
        provider.accept(referenceTestMock);

        Stream<? extends Arguments> arguments =
                assertDoesNotThrow(() -> provider.provideArguments(mockDeclaration, contextMock));
        assertEquals(2, arguments.count());
    }

    /**
     * This test checks that provideArguments creates a single test case when the path points to a single file.
     */
    @Test
    public void testProvideArgumentsWithSingleFile() {
        String path = "io/transconnect/connector/test/reference/ReferenceTestConsumer/test1.xml";

        // Mock the Context
        ExtensionContext contextMock = mock(ExtensionContext.class);

        // prepare the provider
        ReferenceTest referenceTestMock = mock(ReferenceTest.class);
        ParameterDeclarations mockDeclaration = mock(ParameterDeclarations.class);
        when(referenceTestMock.path()).thenReturn(path);
        when(referenceTestMock.consumer()).thenAnswer(invocation -> MockConsumerConnector.class);
        when(referenceTestMock.producer()).thenAnswer(invocation -> DefaultProducerConnector.class);
        provider.accept(referenceTestMock);

        Stream<? extends Arguments> arguments =
                assertDoesNotThrow(() -> provider.provideArguments(mockDeclaration, contextMock));
        assertEquals(1, arguments.count());
    }
}
