/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.transconnect.connector.test.reference.ReferenceTestException.ReferenceError;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class XmlValidatorTest {

    @Test
    public void testValidationXmlWellFormed() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/sample/execute/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/hello classpath:hello.xsd">
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertNotSame(ReferenceError.WELL_FORMED_READ, exception.getError());
    }

    /**
     * Test for failing if the xml is not well-formed.
     * Throws an error in test output
     */
    @Test
    public void testValidationXmlNotWellFormed() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <message
                xmlns:test="http://transconnect.io/connector/test"
                xmlns="http://transconnect.io/connector/sample/execute/hello"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://transconnect.io/hello classapth:hello.xsd">
            <message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.WELL_FORMED_READ, exception.getError());
    }

    @Test
    public void testMissingXsdFile() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/missing"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/missing classpath:missing.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.MISSING_XSD_FILE_ON_CLASSPATH, exception.getError());
    }

    @Test
    public void testMissingRelativeXsdFile() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/missing"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/missing ../missing.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.MISSING_XSD_FILE, exception.getError());
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }

    @Test
    public void testMissingXsdFileResourceDirectory() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/missing"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/missing resources/missing.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.MISSING_XSD_FILE_ON_CLASSPATH_FROM_RELATIVE, exception.getError());
    }

    @Test
    public void testMissingXsdFileWrongProtocol() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/missing"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/missing jar:missing.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.UNSUPPORTED_PROTOCOL, exception.getError());
    }

    @Test
    public void testXsdRelativeResourcePath() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/connector/hello ../../../../../../../resources/io/transconnect/connector/test/reference/xsd/hello.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        assertDoesNotThrow(
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
    }

    @Test
    public void testXsdRelativeNoResourcePath() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/connector/hello ../xsd/hello.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                        </output>
                    </message>
            """;
        String xmlPath = "io/transconnect/connector/test/reference/xml/test.xml";
        Path path = ClasspathUtils.convertClasspathToRegularPath(xmlPath);
        assertDoesNotThrow(() -> XmlValidator.validateXmlSchema(
                new ByteArrayInputStream(validXml.getBytes()),
                path.toAbsolutePath().toString()));
    }

    @Test
    public void testMissingTestConfiguration() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/hello classpath:hello.xsd">
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.TEST_EXTRACTION, exception.getError());
    }

    @Test
    public void testNoDefaultNamespace() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/missing classpath:missing.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;

        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.NO_DEFAULT_NAMESPACE, exception.getError());
    }

    @Test
    public void testNoSchemaLocation() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/sample/execute/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.NO_XSI_SCHEMA_LOCATION, exception.getError());
    }

    @Test
    public void testNoSchemaLocationsPairs() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/hello classpath:hello.xsd
                                http://transconnect.io/goodbye"
                                >
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.INVALID_SCHEMA_PARSING, exception.getError());
    }

    @Test
    public void testNoSchemaNamespaceMapping() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hellogoodbye"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/hello classpath:hello.xsd
                                http://transconnect.io/goodbye goodbye.xsd"
                                >
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp test:ignore="true">1000</timestamp>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.NO_NAMESPACE_SCHEMA_MAPPING, exception.getError());
    }

    @Test
    public void testValidateInputOutputValidXml() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/connector/hello classpath:io/transconnect/connector/test/reference/xsd/hello.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                        </output>
                    </message>
            """;

        String xmlPath = "/home/test/test.xml";
        Document document = assertDoesNotThrow(
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertNotNull(document);
    }

    @Test
    public void testValidateInputInvalidPayload() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/connector/hello classpath:io/transconnect/connector/test/reference/xsd/hello.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                            <timestamp>12345</timestamp>
                        </output>
                    </message>
            """;

        String xmlPath = "/home/test/test.xml";
        ReferenceTestException exception = assertThrows(
                ReferenceTestException.class,
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertSame(ReferenceError.INVALID_PAYLOAD, exception.getError());
    }

    @Test
    public void testValidatePayloadRemoveAttributesByNamespace() {
        String validXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                    <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="
                                http://transconnect.io/connector/hello classpath:io/transconnect/connector/test/reference/xsd/hello.xsd">
                        <test:configuration>
                            <test:property id="uppercase">true</test:property>
                            <test:property id="retryCount">2</test:property>
                            <test:property id="language">German</test:property>
                            <test:property id="resultInAttachment">false</test:property>
                        </test:configuration>
                        <input test:interaction="hello">
                             <name>Tütütü</name>
                        </input>
                        <output>
                            <greeting test:ignore="true">HALLO TÜTÜTÜ</greeting>
                        </output>
                    </message>
            """;
        String xmlPath = "/home/test/test.xml";
        Document document = assertDoesNotThrow(
                () -> XmlValidator.validateXmlSchema(new ByteArrayInputStream(validXml.getBytes()), xmlPath));
        assertNotNull(document);
    }
}
