/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.jaxb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.transconnect.connector.MockMessage;
import io.transconnect.connector.MockWritableMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JaxbExtensionTest {

    private JaxbExtension extension;

    @BeforeEach
    void setUp() {
        extension = new JaxbExtension();
    }

    @Test
    void unmarshalMessage_validXml_returnsObject() throws Exception {
        // Given
        String xml = "<?xml version=\"1.0\""
                + " encoding=\"UTF-8\"?><testObject><name>test</name><value>123</value></testObject>";
        MockMessage message = new MockMessage(xml);

        // When
        TestObject result = extension.unmarshalMessage(message, TestObject.class, "schema/execute_in.xsd", null);

        // Then
        assertNotNull(result);
        assertEquals("test", result.name);
        assertEquals(123, result.value);
    }

    @Test
    void unmarshalMessage_invalidXml_throwsException() {
        // Given
        String invalidXml = "invalid xml content";
        MockMessage message = new MockMessage(invalidXml);

        // When/Then
        assertThrows(
                RuntimeException.class,
                () -> extension.unmarshalMessage(message, TestObject.class, "schema/execute_in.xsd", null));
    }

    @Test
    void marshalMessage_validObject_createsXml() throws Exception {
        // Given
        TestObject testObject = new TestObject();
        testObject.name = "test";
        testObject.value = 123;
        MockWritableMessage message = new MockWritableMessage();

        // When
        extension.marshalMessage(message, testObject, null);

        // Then
        assertEquals("""
            <?xml version="1.0" encoding="UTF-8"?>
            <testObject>
               <name>test</name>
               <value>123</value>
            </testObject>
            """, new String(message.getBody()));
    }

    @Test
    void clearCaches_doesNotThrowException() {
        // When/Then
        extension.clearCaches();
    }
}
