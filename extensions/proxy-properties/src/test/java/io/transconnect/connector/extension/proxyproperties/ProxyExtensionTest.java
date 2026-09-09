/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.proxyproperties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import org.testng.annotations.Test;

public class ProxyExtensionTest {

    @Test
    public void testModifyDescriptionConsumer() {
        var cut = new io.transconnect.connector.extension.proxyproperties.ProxyExtension();
        var desc = new ConsumerConnectorDescriptor();
        var modDesc = cut.modifyDescription(desc);
        assertNotNull(modDesc);
        assertEquals(modDesc.getCommon(), desc.getCommon());
        assertEquals(modDesc.getInteractions(), desc.getInteractions());
        assertEquals(modDesc.getProperties().length, 4);
        assertEquals(
                modDesc.getProperties()[0].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_HOST);
        assertEquals(
                modDesc.getProperties()[1].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PORT);
        assertEquals(
                modDesc.getProperties()[2].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_USER);
        assertEquals(
                modDesc.getProperties()[3].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PASSWORD);
    }

    @Test
    public void testModifyDescriptionProducer() {
        var cut = new io.transconnect.connector.extension.proxyproperties.ProxyExtension();
        var desc = new ProducerConnectorDescriptor();
        var modDesc = cut.modifyDescription(desc);
        assertNotNull(modDesc);
        assertEquals(modDesc.getCommon(), desc.getCommon());
        assertEquals(modDesc.getProperties().length, 4);
        assertEquals(
                modDesc.getProperties()[0].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_HOST);
        assertEquals(
                modDesc.getProperties()[1].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PORT);
        assertEquals(
                modDesc.getProperties()[2].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_USER);
        assertEquals(
                modDesc.getProperties()[3].getId(),
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PASSWORD);
    }

    @Test
    public void testReadConfiguration() {
        var cut = new io.transconnect.connector.extension.proxyproperties.ProxyExtension();
        var config = new MockConfig();
        config.setValue(
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_HOST, "localhost");
        config.setValue(io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PORT, 8080L);
        config.setValue(io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_USER, "user");
        config.setValue(
                io.transconnect.connector.extension.proxyproperties.ProxyExtension.PROPERTY_PROXY_PASSWORD, "password");

        cut.readConfiguration(config);

        assertEquals(cut.getHost(), "localhost");
        assertEquals(cut.getPort(), Long.valueOf(8080L));
        assertEquals(cut.getUsername(), "user");
        assertEquals(cut.getPassword(), "password");
    }
}
