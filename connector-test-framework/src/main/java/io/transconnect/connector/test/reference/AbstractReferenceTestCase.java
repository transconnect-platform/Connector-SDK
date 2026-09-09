/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.MockMessage;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.property.ConnectorPropertyType;
import io.transconnect.connector.api.property.IConnectorProperty;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import org.hamcrest.StringDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.matchers.CompareMatcher;

/**
 * The {@code ReferenceTest} class is designed to facilitate reference testing of a connector. It allows
 * comparison between expected and actual outputs, handling discrepancies by writing them to a file.
 */
public abstract class AbstractReferenceTestCase {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractReferenceTestCase.class);
    /**
     * The reference file path used for the test.
     */
    private final File referenceFile;

    /**
     * The expected result in the test.
     */
    private final String expectedResult;

    /**
     * test prefix.
     */
    private final String testNamespace;

    /**
     * expected attachments of the output message, mapping the attachment id to the referenced file path.
     * The file content is read lazily during the comparison to save memory.
     */
    private final Map<String, String> expectedAttachments;

    /**
     * flag whether the output attachments should be compared.
     * true, if the output attachments should be compared, otherwise false
     */
    private final boolean compareAttachments;

    /**
     * Super constructor for a new ReferenceTestCase object with specified parameters.
     *
     * @param refFile The path to the reference file
     * @param expected The expected result for this test case
     * @param testNamespace The namespace to be used for this test case
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public AbstractReferenceTestCase(File refFile, String expected, String testNamespace) {
        this(refFile, expected, testNamespace, Map.of(), false);
    }

    /**
     * Super constructor for a new ReferenceTestCase object with specified parameters.
     *
     * @param refFile The path to the reference file
     * @param expected The expected result for this test case
     * @param testNamespace The namespace to be used for this test case
     * @param expectedAttachments The expected output attachments (id to referenced file path)
     * @param compareAttachments true, if the output attachments should be compared, otherwise false
     * @throws IllegalArgumentException if any parameter is null or invalid
     */
    public AbstractReferenceTestCase(
            File refFile,
            String expected,
            String testNamespace,
            Map<String, String> expectedAttachments,
            boolean compareAttachments) {

        if (refFile == null) {
            throw new IllegalArgumentException("Reference file cannot be null");
        }
        if (expected == null) {
            throw new IllegalArgumentException("Expected result cannot be null");
        }
        if (testNamespace == null) {
            throw new IllegalArgumentException("Test namespace cannot be null");
        }

        referenceFile = refFile;
        expectedResult = expected;
        this.testNamespace = testNamespace;
        this.expectedAttachments = expectedAttachments != null ? expectedAttachments : Map.of();
        this.compareAttachments = compareAttachments;
    }

    /**
     * Executes the test and returns a message from the connector. This method must be implemented by subclasses.
     * @return The result message of the connector's execution
     * @throws ReferenceTestException If an error occurs during the test execution.
     */
    public abstract Message run() throws ReferenceTestException;

    /**
     * Ignore the namespace while comparison.
     * @return true, if namespace should be ignored, otherwise false
     */
    protected abstract boolean ignoreCompareNamespace();

    /**
     * Compares the output message with the expected string and writes differences to a file if discrepancies are found.
     * @param output The result output message of the test.
     * @return A {@code MockMessage} representing the compared output
     * @throws ReferenceTestException If an error occurs during comparison or writing differences.
     */
    public Message compareResults(Message output) throws ReferenceTestException {

        // result to compare
        String result;
        MockMessage returnMessage;

        XMLEventWriter eventWriter = null;
        try (StringWriter writer = new StringWriter()) {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            eventWriter = outputFactory.createXMLEventWriter(writer);
            eventWriter.add(output.getXmlBody());
            eventWriter.flush();
            result = writer.toString();

            // create a new MockMessage to return the message
            returnMessage = new MockMessage(result);

        } catch (Exception e) {
            throw new ReferenceTestException("Could not create String of the XMLBody", e);
        } finally {
            if (eventWriter != null) {
                try {
                    eventWriter.close();
                } catch (Exception exception) {
                    logger.debug("Could not close the XMLEventWriter during comparing results", exception);
                }
            }
        }

        // compare with XMLUnit
        CompareMatcher matcher = CompareMatcher.isIdenticalTo(expectedResult)
                .ignoreComments()
                .ignoreWhitespace()
                .withDifferenceEvaluator(new IgnoreSubtreeEvaluator(testNamespace, ignoreCompareNamespace()));

        if (!matcher.matches(result)) {
            StringDescription description = new StringDescription();
            matcher.describeMismatch(result, description);
            throw new ReferenceTestException(
                    "Differences detected: " + referenceFile.getAbsolutePath() + System.lineSeparator() + description);
        }

        // compare the expected output attachments against the ones produced by the connector
        if (compareAttachments) {
            compareOutputAttachments(output);
        }

        return returnMessage;
    }

    /**
     * Compares the expected output attachments against the attachments of the produced message. The set of
     * attachments must match exactly: every expected attachment must be present and match byte for byte, and the
     * result message must not contain any attachment that is not expected.
     * @param output The result output message of the test.
     * @throws ReferenceTestException if an expected attachment is missing, its content differs or the result
     *         message contains an unexpected attachment.
     */
    private void compareOutputAttachments(Message output) throws ReferenceTestException {

        // the result must not contain any attachment that is not declared as expected
        for (String actualId : output.getAttachmentIds()) {
            if (!expectedAttachments.containsKey(actualId)) {
                throw new ReferenceTestException("Unexpected attachment '" + actualId + "' in the result message: "
                        + referenceFile.getAbsolutePath());
            }
        }

        // every expected attachment must be present and its content must match byte for byte
        for (Map.Entry<String, String> expected : expectedAttachments.entrySet()) {
            String id = expected.getKey();

            if (!output.getAttachmentIds().contains(id)) {
                throw new ReferenceTestException("Expected attachment '" + id + "' is missing in the result message: "
                        + referenceFile.getAbsolutePath());
            }

            // the expected attachment file is only read here, at comparison time, to save memory
            byte[] expectedContent;
            try {
                expectedContent = Files.readAllBytes(Paths.get(expected.getValue()));
            } catch (IOException exception) {
                throw new ReferenceTestException(
                        "Could not read expected attachment file: " + expected.getValue(), exception);
            }

            byte[] actual;
            try (InputStream attachmentStream = output.getAttachment(id)) {
                actual = attachmentStream.readAllBytes();
            } catch (IOException | IllegalArgumentException exception) {
                throw new ReferenceTestException("Could not read result attachment '" + id + "'", exception);
            }

            if (!Arrays.equals(expectedContent, actual)) {
                throw new ReferenceTestException("Attachment '" + id + "' differs from the expected content: "
                        + referenceFile.getAbsolutePath());
            }
        }
    }

    /**
     * Creates a configuration object from Property meta data and a values map.
     * @param properties An array of {@code IConnectorProperty} objects representing the connector's properties.
     * @param config A map containing string key-value pairs representing the configuration settings.
     * @return A {@code Configuration} object tailored for the connector based on the provided properties and config
     */
    public Configuration mapConnectorProperties(IConnectorProperty[] properties, Map<String, String> config) {

        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(config, "config must not be null");

        Map<String, Object> mapped = new HashMap<>();

        Map<String, ConnectorPropertyType> propertyTypes = Arrays.stream(properties)
                .collect(Collectors.toMap(IConnectorProperty::getId, IConnectorProperty::getType));

        for (var configEntry : config.entrySet()) {
            String name = configEntry.getKey();
            String strValue = configEntry.getValue();

            if (strValue == null) {
                continue;
            }

            // get the type to map the property
            ConnectorPropertyType propertyType = propertyTypes.get(name);
            mapped.put(name, mapValue(propertyType, strValue));
        }

        return new MockConfig(mapped);
    }

    /**
     * maps the value of String to the given property type.
     * @param propertyType the ConnectorPropertyType
     * @param strValue the string value
     * @return the mapped value
     */
    private Object mapValue(ConnectorPropertyType propertyType, String strValue) {

        if (propertyType == null) {
            return strValue;
        }

        Class<?> clazz = propertyType.getClassId();
        Object value;
        if (clazz == String.class) {
            value = strValue;
        } else if (clazz == Boolean.class) {
            value = Boolean.valueOf(strValue);
        } else if (clazz == Integer.class) {
            value = Long.valueOf(strValue);
        } else if (clazz == Float.class) {
            value = Double.valueOf(strValue);
        } else if (clazz == Double.class) {
            value = Double.valueOf(strValue);
        } else if (clazz == PrivateKey.class) {
            value = decodePrivateKey(strValue);
        } else if (clazz == Certificate.class) {
            value = decodeCertificate(strValue);
        } else {
            value = strValue;
        }
        return value;
    }

    /**
     * Decodes a Base64 encoded key string into a {@code PrivateKey} object using RSA algorithm.
     * @param base64EncodedKey The Base64 encoded key string to be decoded.
     * @return A {@code PrivateKey} object corresponding to the provided Base64 encoded key
     */
    private PrivateKey decodePrivateKey(String base64EncodedKey) {

        Objects.requireNonNull(base64EncodedKey, "base64EncodedKey must not be null");

        String cleanBase64EncodedKey = filterBase64Part(base64EncodedKey);

        if (!isBase64Encoded(cleanBase64EncodedKey)) {
            throw new IllegalArgumentException("The encoded key is not Base64 encoded");
        }

        try {
            byte[] encodedKey = Base64.getDecoder().decode(cleanBase64EncodedKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            logger.debug("Failed to decode private key: {}", exception.getMessage());
            throw new IllegalArgumentException("Failed to decode private key", exception);
        }
    }

    /**
     * Filters out the Base64 part of a string, removing header and footer lines.
     * @param base64 The input string containing potentially encoded data in Base64 format.
     * @return A cleaned version of the input string without headers or footers
     */
    private String filterBase64Part(String base64) {
        return base64.replaceAll("-----BEGIN [A-Z0-9 ]+-----|-----END [A-Z0-9 ]+-----", "")
                .replaceAll("\n", "");
    }

    /**
     * Decodes a Base64 encoded certificate string into an {@code Certificate} object using the X.509 algorithm.
     * @param base64EncodedCert The Base64 encoded certificate string to be decoded.
     * @return An {@code Certificate} object corresponding to the provided Base64 encoded certificate
     */
    private Certificate decodeCertificate(String base64EncodedCert) {

        Objects.requireNonNull(base64EncodedCert, "base64EncodedCert must not be null");

        String cleanBase64EncodedCert = filterBase64Part(base64EncodedCert);
        if (!isBase64Encoded(cleanBase64EncodedCert)) {
            throw new IllegalArgumentException("The encoded certificate is not Base64 encoded");
        }

        try {
            byte[] decodedCert = Base64.getDecoder().decode(cleanBase64EncodedCert);
            return CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(decodedCert));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode certificate", e);
        }
    }

    /**
     * Checks if a given string is Base64 encoded by attempting to decode it and catching any exceptions
     * that occur due to invalid encoding.
     * @param encoded The input string which may or may not be Base64 encoded.
     * @return {@code true} if the string appears to be Base64 encoded, otherwise {@code false}
     */
    private boolean isBase64Encoded(String encoded) {
        if (encoded == null) {
            return false;
        }
        try {
            Base64.getDecoder().decode(encoded);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
