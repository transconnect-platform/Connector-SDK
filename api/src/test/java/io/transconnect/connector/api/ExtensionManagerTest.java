/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.api;

import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.extension.ConnectorExtension;
import io.transconnect.connector.api.extension.DescriptorExtension;
import io.transconnect.connector.api.extension.ExtensionManager;
import io.transconnect.connector.api.mocks.MockConnectorWithExtension;
import io.transconnect.connector.api.mocks.MockConnectorWithExtensions;
import io.transconnect.connector.api.mocks.MockConnectorWithoutExtensions;
import io.transconnect.connector.api.mocks.MockExtensionSecond;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test class for the ExtensionManager.
 */
public class ExtensionManagerTest {

    /**
     * Check getExtensions() if there is no annotation.
     * @throws TransconnectConnectorException test fails
     */
    @Test
    public void testGetExtensionsNoExtension() throws TransconnectConnectorException {

        ExtensionManager manager = new ExtensionManager(MockConnectorWithoutExtensions.class);
        List<ConnectorExtension> extensions = manager.getExtensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertTrue(extensions.isEmpty(), "Extensions should be empty");
    }

    /**
     * Check with one given extension in the annotation.
     */
    @Test
    public void testGetExtensionsWithExtension() {
        ExtensionManager manager = new ExtensionManager(MockConnectorWithExtension.class);
        List<ConnectorExtension> extensions = Assertions.assertDoesNotThrow(manager::getExtensions);
        Assertions.assertEquals(1, extensions.size());
    }

    /**
     * Verify that the getExtensions - Method returns a list of extensions.
     */
    @Test
    public void testGetExtensionsWithExtensions() {
        ExtensionManager manager = new ExtensionManager(MockConnectorWithExtensions.class);
        List<ConnectorExtension> extensions = Assertions.assertDoesNotThrow(manager::getExtensions);
        Assertions.assertEquals(2, extensions.size());
    }

    /**
     * Verify that the getExtensionsByType returns a list with more than one extension.
     */
    @Test
    void testGetExtensionsExtensionByType() {
        ExtensionManager manager = new ExtensionManager(MockConnectorWithExtensions.class);
        List<DescriptorExtension> extList =
                Assertions.assertDoesNotThrow(() -> manager.getExtensionsByType(DescriptorExtension.class));
        Assertions.assertEquals(2, extList.size());
    }

    /**
     * Verifies that {@link ExtensionManager#getExtensionsByType(Class)}.
     * gives an empty list if the type is not available
     */
    @Test
    void testGetExtensionsWithNoTypeAvailable() {
        ExtensionManager manager = new ExtensionManager(MockConnectorWithoutExtensions.class);
        List<MockExtensionSecond> extList =
                Assertions.assertDoesNotThrow(() -> manager.getExtensionsByType(MockExtensionSecond.class));

        Assertions.assertEquals(0, extList.size());
    }

    /**
     * Verifies that {@link ExtensionManager#applyExtensions(ProducerConnectorDescriptor)}.
     * correctly orchestrates descriptor extensions.
     */
    @Test
    void testApplyExtensionsWithProducer() throws TransconnectConnectorException {

        ProducerConnectorDescriptor d0 = Mockito.mock(ProducerConnectorDescriptor.class);
        ProducerConnectorDescriptor d1 = Mockito.mock(ProducerConnectorDescriptor.class);
        ProducerConnectorDescriptor d2 = Mockito.mock(ProducerConnectorDescriptor.class);

        DescriptorExtension ext1 = Mockito.mock(DescriptorExtension.class);
        DescriptorExtension ext2 = Mockito.mock(DescriptorExtension.class);

        Mockito.when(ext1.modifyDescription(d0)).thenReturn(d1);
        Mockito.when(ext2.modifyDescription(d1)).thenReturn(d2);

        ExtensionManager manager = Mockito.spy(new ExtensionManager(MockConnectorWithExtensions.class));
        Mockito.doReturn(List.of(ext1, ext2)).when(manager).getExtensionsByType(DescriptorExtension.class);

        ProducerConnectorDescriptor out = manager.applyExtensions(d0);
        Assertions.assertSame(d2, out);
    }

    /**
     * Verifies that {@link ExtensionManager#applyExtensions(ConsumerConnectorDescriptor)}.
     * correctly orchestrates descriptor extensions.
     */
    @Test
    void testApplyExtensionsWithConsumer() throws TransconnectConnectorException {

        ConsumerConnectorDescriptor d0 = Mockito.mock(ConsumerConnectorDescriptor.class);
        ConsumerConnectorDescriptor d1 = Mockito.mock(ConsumerConnectorDescriptor.class);
        ConsumerConnectorDescriptor d2 = Mockito.mock(ConsumerConnectorDescriptor.class);

        DescriptorExtension ext1 = Mockito.mock(DescriptorExtension.class);
        DescriptorExtension ext2 = Mockito.mock(DescriptorExtension.class);

        Mockito.when(ext1.modifyDescription(d0)).thenReturn(d1);
        Mockito.when(ext2.modifyDescription(d1)).thenReturn(d2);

        ExtensionManager manager = Mockito.spy(new ExtensionManager(MockConnectorWithExtensions.class));
        Mockito.doReturn(List.of(ext1, ext2)).when(manager).getExtensionsByType(DescriptorExtension.class);

        ConsumerConnectorDescriptor out = manager.applyExtensions(d0);
        Assertions.assertSame(d2, out);
    }
}
