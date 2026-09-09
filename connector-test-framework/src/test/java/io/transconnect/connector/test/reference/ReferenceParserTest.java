/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReferenceParserTest {
    private ReferenceParser referenceParser;

    @BeforeEach
    public void before() {
        referenceParser = new ReferenceParser();
    }

    /**
     * This test checks the parsing of a valid configuration section-
     */
    @Test
    public void testParseValidConfiguration() {

        String validConfig = """
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
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("uppercase", "true");
        expectedMap.put("retryCount", "2");
        expectedMap.put("language", "German");
        expectedMap.put("resultInAttachment", "false");

        assertDoesNotThrow(() -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
        Map<String, String> config = referenceParser.getConfiguration();
        assertEquals(expectedMap, config);
    }

    @Test
    public void testParseValidConsumer() {

        String validConfig = """
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
        assertDoesNotThrow(() -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
        assertTrue(referenceParser.isConsumer());
    }

    @Test
    public void testParseValidProducer() {

        String validConfig = """
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
                    <output>
                        <greeting>HALLO TÜTÜTÜ</greeting>
                    </output>
                </message>
            """;
        String xmlPath = "/home/test/test.xml";
        assertDoesNotThrow(() -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
        assertFalse(referenceParser.isConsumer());
    }

    @Test
    public void testParseEmptyInteraction() {

        String validConfig = """
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
                    <input test:interaction="  ">
                         <name>Tütütü</name>
                    </input>
                    <output>
                        <greeting>HALLO TÜTÜTÜ</greeting>
                    </output>
                </message>
            """;
        String xmlPath = "/home/test/test.xml";
        assertThrows(
                ReferenceTestException.class,
                () -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
    }

    @Test
    public void testGetInputMessage() {

        String validConfig = """
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

        String expected = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                 <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                 <input>
                       <name>Tütütü</name>
                 </input>
                 </message>
            """;
        String xmlPath = "/home/test/test.xml";
        assertDoesNotThrow(() -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
        assertThat(referenceParser.getMessage(true))
                .and(expected)
                .ignoreWhitespace()
                .areIdentical();
    }

    @Test
    public void testGetOutputMessage() {

        String validConfig = """
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

        String expected = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                 <message
                        xmlns:test="http://transconnect.io/connector/test"
                        xmlns="http://transconnect.io/connector/hello"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xsi:schemaLocation="
                                    http://transconnect.io/connector/hello classpath:io/transconnect/connector/test/reference/xsd/hello.xsd">
                 <output>
                            <greeting>HALLO TÜTÜTÜ</greeting>
                        </output>
                 </message>
            """;
        String xmlPath = "/home/test/test.xml";
        assertDoesNotThrow(() -> referenceParser.parse(new ByteArrayInputStream(validConfig.getBytes()), xmlPath));
        assertThat(referenceParser.getMessage(false))
                .and(expected)
                .ignoreWhitespace()
                .areIdentical();
    }

    /**
     * This test checks that a test:attachment declared in the input is loaded as bytes and removed from the
     * message that is sent to the connector.
     * @throws Exception if the test fails
     */
    @Test
    public void testParseInputAttachments() throws Exception {

        String resource = "io/transconnect/connector/test/reference/ReferenceTestInputAttachment/consumer-attachment.xml";
        String realPath = ClasspathUtils.convertClasspathToRegularPath(resource).toString();

        try (InputStream in = ClasspathUtils.loadResource(resource)) {
            referenceParser.parse(in, realPath);
        }

        // the referenced file is loaded and provided as bytes under its id
        Map<String, byte[]> attachments = referenceParser.getInputAttachments();
        assertEquals(1, attachments.size());
        assertArrayEquals("extra content".getBytes(StandardCharsets.UTF_8), attachments.get("additionalText"));

        // the test:attachment element must not be part of the message sent to the connector;
        // regular payload content containing the word "attachment" must not matter
        String inputMessage = referenceParser.getMessage(true);
        assertThat(inputMessage)
                .nodesByXPath("//*[local-name()='attachment' and namespace-uri()='" + XmlValidator.TEST_NAMESPACE
                        + "']")
                .doNotExist();
        assertTrue(inputMessage.contains("Tütütü"), "input body must still contain the payload");
    }

    /**
     * This test checks that a test:attachment declared in the output is loaded as bytes and removed from the
     * expected output that is compared against the connector result.
     * @throws Exception if the test fails
     */
    @Test
    public void testParseOutputAttachments() throws Exception {

        String resource =
                "io/transconnect/connector/test/reference/ReferenceTestOutputAttachment/producer-attachment.xml";
        String realPath = ClasspathUtils.convertClasspathToRegularPath(resource).toString();

        try (InputStream in = ClasspathUtils.loadResource(resource)) {
            referenceParser.parse(in, realPath);
        }

        // only the file reference is stored; the content is read lazily during the comparison
        Map<String, String> attachments = referenceParser.getOutputAttachments();
        assertEquals(1, attachments.size());
        assertArrayEquals(
                "result bytes".getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(Paths.get(attachments.get("result"))));

        // the test:attachment element must not be part of the expected output that is compared;
        // regular payload content containing the word "attachment" must not matter
        String outputMessage = referenceParser.getMessage(false);
        assertThat(outputMessage)
                .nodesByXPath("//*[local-name()='attachment' and namespace-uri()='" + XmlValidator.TEST_NAMESPACE
                        + "']")
                .doNotExist();
        assertTrue(outputMessage.contains("HALLO TÜTÜTÜ"), "output body must still contain the payload");
    }
}
