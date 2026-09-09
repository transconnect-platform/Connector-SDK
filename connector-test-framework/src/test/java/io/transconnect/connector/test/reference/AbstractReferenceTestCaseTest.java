/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.transconnect.connector.MockMessage;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.property.ConnectorProperty;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class AbstractReferenceTestCaseTest {

    private TestableAbstractReferenceTestCase testableReferenceTestCase;
    private File referenceFile;

    /**
     * temporary directory, created fresh per test and cleaned up automatically.
     */
    @TempDir
    private Path tempDir;

    private final String testNamespace = "http://transconnect.io/connector/test";

    /**
     * XML body used for the attachment comparison tests.
     */
    private static final String OUTPUT_BODY = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
            + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
            + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
            + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
            + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">HALLO"
            + " MARTIN</greeting></message>";

    /**
     * setup before test method.
     * @throws Exception if an error occurs
     */
    @BeforeEach
    public void before() throws Exception {
        // create mocks and variables
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
                + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
                + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">HALLO"
                + " MARTIN</greeting></message>";

        referenceFile = tempDir.resolve("reference.xml").toFile();

        testableReferenceTestCase =
                Mockito.spy(new TestableAbstractReferenceTestCase(referenceFile, expectedResult, testNamespace));
    }

    /**
     * Tests the compareAndWriteDiff method with identically outputs
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsNoDifferences() throws Exception {
        String actualResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
                + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
                + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">HALLO"
                + " MARTIN</greeting></message>";
        Message actualMessage = createMockMessage(actualResult);

        // actual result message
        Message resultMessage = testableReferenceTestCase.compareResults(actualMessage);
        assertNotNull(resultMessage);
        assertFalse(referenceFile.exists());
    }

    /**
     * Tests the compareAndWriteDiff method with different outputs
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithDifferences() throws Exception {
        String actualResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
                + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
                + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">HALLO"
                + " MARTIN</greeting><timestamp>1000</timestamp></message>";

        Message actualMessage = createMockMessage(actualResult);

        assertThrows(ReferenceTestException.class, () -> testableReferenceTestCase.compareResults(actualMessage));
    }

    /**
     * Tests the compareAndWriteDiff method with invalid xml
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithException() throws Exception {
        // use invalid xml
        String actualResult = "<actual><data>invalidXML";
        Message mockMessage = createMockMessage(actualResult);

        assertThrows(ReferenceTestException.class, () -> {
            testableReferenceTestCase.compareResults(mockMessage);
        });
    }

    /**
     * Tests the compareAndWriteDiff method with ignoring nodes
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithIgnoreNodes() throws Exception {
        String ignoreResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
            + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
            + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
            + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + " xsi:type=\"xsd:string\">HALLO MARTIN</greeting><timestamp"
            + " test:ignore=\"true\">1000</timestamp><ignored test:ignore=\"true\">TestKäse</ignored></message>";

        testableReferenceTestCase = new TestableAbstractReferenceTestCase(referenceFile, ignoreResult, testNamespace);

        String actualResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><message"
                + " xmlns=\"http://transconnect.io/connector/sample/execute/out\""
                + " xmlns:test=\"http://transconnect.io/connector/test\"><greeting"
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">HALLO"
                + " MARTIN</greeting><timestamp>1001</timestamp><ignored>TestKäse</ignored></message>";

        Message actualMessage = createMockMessage(actualResult);

        Message resultMessage = testableReferenceTestCase.compareResults(actualMessage);

        assertNotNull(resultMessage);
    }

    /**
     * Tests that a matching output attachment passes the comparison when compareAttachments is enabled.
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithMatchingAttachment() throws Exception {
        Map<String, String> expected = Map.of("result", writeAttachmentFile("match-expected.bin", "content"));
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, expected, true);

        MockMessage output = new MockMessage(OUTPUT_BODY);
        output.addAttachment("result", "content".getBytes(StandardCharsets.UTF_8));

        assertNotNull(testCase.compareResults(output));
    }

    /**
     * Tests that a differing output attachment fails the comparison when compareAttachments is enabled.
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithDifferentAttachment() throws Exception {
        Map<String, String> expected = Map.of("result", writeAttachmentFile("different-expected.bin", "content"));
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, expected, true);

        MockMessage output = new MockMessage(OUTPUT_BODY);
        output.addAttachment("result", "different".getBytes(StandardCharsets.UTF_8));

        assertThrows(ReferenceTestException.class, () -> testCase.compareResults(output));
    }

    /**
     * Tests that a missing output attachment fails the comparison when compareAttachments is enabled.
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithMissingAttachment() throws Exception {
        Map<String, String> expected = Map.of("result", writeAttachmentFile("missing-expected.bin", "content"));
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, expected, true);

        MockMessage output = new MockMessage(OUTPUT_BODY);

        assertThrows(ReferenceTestException.class, () -> testCase.compareResults(output));
    }

    /**
     * Tests that the comparison fails when the connector returns fewer attachments than expected.
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsWithFewerAttachmentsThanExpected() throws Exception {
        Map<String, String> expected = Map.of(
                "first", writeAttachmentFile("fewer-first.bin", "first content"),
                "second", writeAttachmentFile("fewer-second.bin", "second content"));
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, expected, true);

        // the connector only returns one of the two expected attachments
        MockMessage output = new MockMessage(OUTPUT_BODY);
        output.addAttachment("first", "first content".getBytes(StandardCharsets.UTF_8));

        assertThrows(ReferenceTestException.class, () -> testCase.compareResults(output));
    }

    /**
     * Tests that an unexpected (not declared) output attachment fails the comparison when compareAttachments is
     * enabled, even if no attachment was expected.
     */
    @Test
    public void testCompareResultsWithUnexpectedAttachment() {
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, Map.of(), true);

        MockMessage output = new MockMessage(OUTPUT_BODY);
        output.addAttachment("unexpected", "content".getBytes(StandardCharsets.UTF_8));

        assertThrows(ReferenceTestException.class, () -> testCase.compareResults(output));
    }

    /**
     * Tests that attachments are not compared when compareAttachments is disabled, even if they differ.
     * @throws Exception if the test fails
     */
    @Test
    public void testCompareResultsAttachmentsNotComparedWhenDisabled() throws Exception {
        Map<String, String> expected = Map.of("result", writeAttachmentFile("disabled-expected.bin", "content"));
        TestableAbstractReferenceTestCase testCase =
                new TestableAbstractReferenceTestCase(referenceFile, OUTPUT_BODY, testNamespace, expected, false);

        // the result message has no attachment at all, but the comparison is disabled
        MockMessage output = new MockMessage(OUTPUT_BODY);

        assertNotNull(testCase.compareResults(output));
    }

    /**
     * Tests the mapping of configuration to property types.
     */
    @Test
    public void testMapConnectorPropertiesBasic() {

        // create the connector properties reference
        IConnectorProperty stringProperty = ConnectorProperty.builder()
                .id("stringTest")
                .type(ConnectorPropertyType.TEXT)
                .build();
        IConnectorProperty integerProperty = ConnectorProperty.builder()
                .id("integerTest")
                .type(ConnectorPropertyType.INTEGER)
                .build();
        IConnectorProperty floatProperty = ConnectorProperty.builder()
                .id("floatTest")
                .type(ConnectorPropertyType.FLOAT)
                .build();
        IConnectorProperty booleanProperty = ConnectorProperty.builder()
                .id("booleanTest")
                .type(ConnectorPropertyType.BOOLEAN)
                .build();

        IConnectorProperty[] properties = {stringProperty, integerProperty, floatProperty, booleanProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(stringProperty.getId(), "value of String test");
        config.put(integerProperty.getId(), "4");
        config.put(floatProperty.getId(), "3.4385787436754386");
        config.put(booleanProperty.getId(), "True");

        // start the method
        Configuration configuration = testableReferenceTestCase.mapConnectorProperties(properties, config);

        // check the configuration
        String propertyStringValue =
                configuration.getString(stringProperty.getId()).orElse("String value does not match");
        assertEquals("value of String test", propertyStringValue);

        Long propertyIntegerValue =
                configuration.getLong(integerProperty.getId()).orElse(0L);
        assertEquals(4, propertyIntegerValue);

        Double propertyDoubleValue =
                configuration.getDouble(floatProperty.getId()).orElse(0.000);
        assertEquals(3.4385787436754386, propertyDoubleValue);

        Boolean propertyBooleanValue =
                configuration.getBoolean(booleanProperty.getId()).orElse(false);
        assertTrue(propertyBooleanValue);
    }

    /**
     * Tests the mapping to property types with wrong Integer value
     */
    @Test
    public void testMapConnectorPropertiesBasicWrongIntegerType() {

        // create the connector properties reference
        IConnectorProperty integerProperty = ConnectorProperty.builder()
                .id("integerTest")
                .type(ConnectorPropertyType.INTEGER)
                .build();

        IConnectorProperty[] properties = {integerProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(integerProperty.getId(), "true");

        // start the method
        assertThrows(
                NumberFormatException.class,
                () -> testableReferenceTestCase.mapConnectorProperties(properties, config));
    }

    /**
     * Tests the mapping to property types with wrong Float value
     */
    @Test
    public void testMapConnectorPropertiesBasicWrongFloatType() {

        // create the connector properties reference
        IConnectorProperty floatProperty = ConnectorProperty.builder()
                .id("floatTest")
                .type(ConnectorPropertyType.FLOAT)
                .build();

        IConnectorProperty[] properties = {floatProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(floatProperty.getId(), "false");

        // start the method
        assertThrows(
                NumberFormatException.class,
                () -> testableReferenceTestCase.mapConnectorProperties(properties, config));
    }

    /**
     * Tests the mapping to property types with wrong Boolean value
     */
    @Test
    public void testMapConnectorPropertiesBasicWrongBooleanType() {

        // create the connector properties reference
        IConnectorProperty booleanProperty = ConnectorProperty.builder()
                .id("booleanTest")
                .type(ConnectorPropertyType.BOOLEAN)
                .build();

        IConnectorProperty[] properties = {booleanProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(booleanProperty.getId(), "boolean test");

        // start the method
        Configuration configuration = testableReferenceTestCase.mapConnectorProperties(properties, config);

        Boolean value = configuration.getBoolean(booleanProperty.getId()).orElse(true);
        assertFalse(value);
    }

    /**
     * Tests the mapping of a valid key to private key property
     */
    @Test
    public void testMapConnectorPropertiesValidPrivateKey() {

        // base64 key string
        String base64 = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAN8SpX8Ue80MInkz"
                + "77rvP04cpbsjkCJqAtxwmyEg5EVucKYO5eXWjdLwVNX4AcvNNOquzLgSIVs8RRG5"
                + "PrbyvbAiYQnfRMbmSz8cKLMw7u6jIUmityYVwEoI32HgiSHhdOEliBv9DVJyZHnz"
                + "Qh02esC6i/jVCuBEJIsOdurfIE0NAgMBAAECgYBD+EmHFDW4v8bNSX71ZarTYTU6"
                + "dVaYtdaTVpheGsiTQ1h9sLJqOtrR+DrRS5U4hRlnnMVyHMwfIZkmElzCn+od7EX2"
                + "d68Pre9U/TCFCIllq2xgAWBL+RFbT4ffjnlZyo3pnPwpYtWL64Ax6kELRo5jW6QH"
                + "Ql6zoP3sp4gjChVxIQJBAPnNFSrsjekeJVCJID58dETpDGt4m3p/eTS7Ix5vGDSx"
                + "NH+iUIkFvG6jpYaU12afGeTjS50Ok99X94P1MnChnGUCQQDkm8Q5aHk6uWcD/6St"
                + "HJ7k55i9c5/BK44305ge/uUMuZH4oIzDncLYW0Pznox3phdkiAnc4A5mmYGqDyEN"
                + "P/+JAkEA3ZGMnwAnd4inrGGU2hflwWG9BG576hG7XoxGwC3mGIa9fCBqsr8FvlUk"
                + "8tR+oqWogB8j6HEPtGEASGlqjgCrCQJBAOSOXyZOLzXo+vdidU+l165fZbOzj7rf"
                + "QvbJk8MZcALC3Q/H5DQG3DDXPh6pGyLcaXLXF5U+ZOdq/dn5+j93BpECQBaKzjko"
                + "BupaXkj7DnnXm4jWUZrScPLkpL7tLARAXsrXngi+JfC0lt4O7o2i/bhKNlZVyuWh"
                + "PiVX7Y3rZvFUxNY=";

        // create the connector properties reference
        IConnectorProperty keyProperty = ConnectorProperty.builder()
                .id("keyTest")
                .type(ConnectorPropertyType.PRIVATE_KEY)
                .build();

        IConnectorProperty[] properties = {keyProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(keyProperty.getId(), base64);

        Configuration configuration = testableReferenceTestCase.mapConnectorProperties(properties, config);

        PrivateKey key = configuration.getPrivateKey(keyProperty.getId()).orElse(null);
        assertNotNull(key);
    }

    /**
     * Tests the mapping of an invalid Base64 key to private key property
     */
    @Test
    public void testMapConnectorPropertiesInvalidBase64() {

        // base64 key string
        String base64 = "+MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAN8SpX8Ue80MInkz"
                + "77rvP04cpbsjkCJqAtxwmyEg5EVucKYO5eXWjdLwVNX4AcvNNOquzLgSIVs8RRG5"
                + "PrbyvbAiYQnfRMbmSz8cKLMw7u6jIUmityYVwEoI32HgiSHhdOEliBv9DVJyZHnz"
                + "Qh02esC6i/jVCuBEJIsOdurfIE0NAgMBAAECgYBD+EmHFDW4v8bNSX71ZarTYTU6"
                + "dVaYtdaTVpheGsiTQ1h9sLJqOtrR+DrRS5U4hRlnnMVyHMwfIZkmElzCn+od7EX2"
                + "d68Pre9U/TCFCIllq2xgAWBL+RFbT4ffjnlZyo3pnPwpYtWL64Ax6kELRo5jW6QH"
                + "Ql6zoP3sp4gjChVxIQJBAPnNFSrsjekeJVCJID58dETpDGt4m3p/eTS7Ix5vGDSx"
                + "NH+iUIkFvG6jpYaU12afGeTjS50Ok99X94P1MnChnGUCQQDkm8Q5aHk6uWcD/6St"
                + "HJ7k55i9c5/BK44305ge/uUMuZH4oIzDncLYW0Pznox3phdkiAnc4A5mmYGqDyEN"
                + "P/+JAkEA3ZGMnwAnd4inrGGU2hflwWG9BG576hG7XoxGwC3mGIa9fCBqsr8FvlUk"
                + "8tR+oqWogB8j6HEPtGEASGlqjgCrCQJBAOSOXyZOLzXo+vdidU+l165fZbOzj7rf"
                + "QvbJk8MZcALC3Q/H5DQG3DDXPh6pGyLcaXLXF5U+ZOdq/dn5+j93BpECQBaKzjko"
                + "BupaXkj7DnnXm4jWUZrScPLkpL7tLARAXsrXngi+JfC0lt4O7o2i/bhKNlZVyuWh"
                + "PiVX7Y3rZvFUxNY=";

        // create the connector properties reference
        IConnectorProperty keyProperty = ConnectorProperty.builder()
                .id("keyTest")
                .type(ConnectorPropertyType.PRIVATE_KEY)
                .build();

        IConnectorProperty[] properties = {keyProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(keyProperty.getId(), base64);

        assertThrows(
                IllegalArgumentException.class,
                () -> testableReferenceTestCase.mapConnectorProperties(properties, config));
    }

    /**
     * Tests the mapping of a valid key to private key property with an invalid key spec
     */
    @Test
    public void testMapConnectorPropertiesInvalidKeySpec() {

        // simulate Base64 in wrong key format
        byte[] fakeKeyBytes = new byte[32];
        new Random().nextBytes(fakeKeyBytes);
        String fakeBase64Key = Base64.getEncoder().encodeToString(fakeKeyBytes);

        // create the connector properties reference
        IConnectorProperty keyProperty = ConnectorProperty.builder()
                .id("keyTest")
                .type(ConnectorPropertyType.PRIVATE_KEY)
                .build();

        IConnectorProperty[] properties = {keyProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(keyProperty.getId(), fakeBase64Key);

        assertThrows(
                IllegalArgumentException.class,
                () -> testableReferenceTestCase.mapConnectorProperties(properties, config));
    }

    /**
     * Tests the mapping of a valid certificate to certificate property
     */
    @Test
    public void testMapConnectorPropertiesValidCertificate() {
        String certString = "-----BEGIN CERTIFICATE-----\n"
                + "MIICQDCCAamgAwIBAgIUX++4yqTEil9do+LjytWgNFMX/YcwDQYJKoZIhvcNAQEL\n"
                + "BQAwMTESMBAGA1UEAwwJVGVzdCBDZXJ0MQ4wDAYDVQQKDAVNeU9yZzELMAkGA1UE\n"
                + "BhMCREUwIBcNMjUxMjA2MTU0ODIzWhgPMjEyNTExMTIxNTQ4MjNaMDExEjAQBgNV\n"
                + "BAMMCVRlc3QgQ2VydDEOMAwGA1UECgwFTXlPcmcxCzAJBgNVBAYTAkRFMIGfMA0G\n"
                + "CSqGSIb3DQEBAQUAA4GNADCBiQKBgQD3IMV5VxeUPF6G0g3vYMdo3D5vIxq6j3ry\n"
                + "diKS0+iMZBQzRopiKwUiIgn+TM8sxYVhLyoHFLQPIPyjO4IFhineGSG5NmY6HbFr\n"
                + "+yWbiEqlWSHWfpftYg1KsZPKPZhfzYHCuaOcaQgkCce1m7cW0NCZmSYpR8aKb4dx\n"
                + "+dtpuoY5FwIDAQABo1MwUTAdBgNVHQ4EFgQUPRMq3iJWtJHyNYEOkNOSzG3XUiow\n"
                + "HwYDVR0jBBgwFoAUPRMq3iJWtJHyNYEOkNOSzG3XUiowDwYDVR0TAQH/BAUwAwEB\n"
                + "/zANBgkqhkiG9w0BAQsFAAOBgQBp4bAWfGKEd1Zx1jO0Cew9pJk3eqPLn7SypTT8\n"
                + "46uQ1ssf8fmxIuHwc32oA8sy/nxM669f68qDv0PRmS6GBKKEv0y319no42c1FkxK\n"
                + "CQzMawcjd5wbX1Pfb8VWQTJ1X8AvfJZ9w7HpzSyVRmGL8YWIG1M8xqA2dPcWxTNt\n"
                + "eRKxig==\n"
                + "-----END CERTIFICATE-----\n";

        String certBase64 = certString
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace("[^A-Za-z0-9+/=]", "")
                .replaceAll("\\s+", "");

        // create the connector properties reference
        IConnectorProperty certProperty = ConnectorProperty.builder()
                .id("certTest")
                .type(ConnectorPropertyType.CERTIFICATE)
                .build();

        IConnectorProperty[] properties = {certProperty};

        // create the reference configuration map
        Map<String, String> config = new HashMap<>();
        config.put(certProperty.getId(), certBase64);

        Configuration configuration = testableReferenceTestCase.mapConnectorProperties(properties, config);

        Certificate cert = configuration.getCertificate(certProperty.getId()).orElse(null);
        assertNotNull(cert);
    }

    /**
     * creates a mock message
     * @param xmlContent the xml
     * @return Message
     * @throws Exception if the message could not be created
     */
    private Message createMockMessage(String xmlContent) throws Exception {
        return new MockMessage(xmlContent);
    }

    /**
     * writes an expected attachment file with the given content and returns its absolute path.
     * @param name the file name
     * @param content the file content
     * @return the absolute path of the written file
     * @throws IOException if the file cannot be written
     */
    private String writeAttachmentFile(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file.toAbsolutePath().toString();
    }

    /**
     * concrete implementation of AbstractReferenceTestCase.
     */
    private static class TestableAbstractReferenceTestCase extends AbstractReferenceTestCase {
        protected TestableAbstractReferenceTestCase(File referenceFile, String expectedResult, String testNamespace) {
            super(referenceFile, expectedResult, testNamespace);
        }

        protected TestableAbstractReferenceTestCase(
                File referenceFile,
                String expectedResult,
                String testNamespace,
                Map<String, String> expectedAttachments,
                boolean compareAttachments) {
            super(referenceFile, expectedResult, testNamespace, expectedAttachments, compareAttachments);
        }

        @Override
        public Message run() {
            return null; // not tested in this test
        }

        @Override
        protected boolean ignoreCompareNamespace() {
            return false;
        }
    }

    /**
     * get the written differences file.
     * @return File
     */
    private File getDifferenceFile() {
        File parentDir = referenceFile.getParentFile();
        File diffDir = new File(parentDir, "differences");
        return new File(diffDir, referenceFile.getName());
    }
}
