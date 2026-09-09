/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.test.reference.ReferenceTestException.ReferenceError;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Util class to validate XML Files.
 */
class XmlValidator {

    /**
     * Schema Factory.
     */
    private static final SchemaFactory SCHEMA_FACTORY = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    /**
     * path to test xsd for the test-namespace.
     */
    private static final String TEST_XSD = "io/transconnect/connector/test/reference/test.xsd";

    /**
     * xsi namespace for schema locations.
     */
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * test namespace.
     */
    public static final String TEST_NAMESPACE = "http://transconnect.io/connector/test";

    /**
     * Util private Constructor.
     * This constructor is marked as private to prevent instantiation of the utility class.
     */
    private XmlValidator() {
        // Util
    }

    /**
     * Validates an XML against test.xsd and given xsd of connector.
     * It is a two-step validation against the test namespace and the namespace used in connector
     *
     * @param xmlStream An InputStream representing the XML file to be validated.
     * @param xmlPath the path of the XML file, used to resolve relative schema locations
     * @return the parsed and validated document
     * @throws ReferenceTestException if the XML is not valid
     */
    public static Document validateXmlSchema(InputStream xmlStream, String xmlPath) throws ReferenceTestException {

        Objects.requireNonNull(xmlStream, "Missing xmlStream");

        try {

            // validate the well-formed character
            Document document = validateWellFormed(xmlStream, xmlPath);

            // validating the test namespace
            validateTestNamespace(document);

            // get the reference Xsd from metadata
            URL url = readConnectorXsd(document);

            Schema schema = SCHEMA_FACTORY.newSchema(url);
            Validator validator = schema.newValidator();

            // validating the input
            validateDocument(validator, document, "input");

            // validating the output
            validateDocument(validator, document, "output");

            // return the document for later use
            return document;

        } catch (SAXException exception) {
            throw new ReferenceTestException(
                    ReferenceError.VALIDATION_FAILED,
                    "The schema for the given connector xsd could not be created",
                    exception);
        }
    }

    /**
     * Extracts the schema location of the connector XSD from the given XML document.
     * <p>
     * The method performs the following steps:
     * <ul>
     *   <li>Reads the default namespace declared via {@code xmlns="..."} on the root element
     *       (with a fallback to the root element namespace).</li>
     *   <li>Parses the {@code xsi:schemaLocation} attribute of the root element.</li>
     *   <li>Returns the schema location that corresponds to the default namespace.</li>
     * </ul>
     *
     * This method does <strong>not</strong> resolve or validate the returned schema location;
     * it only extracts the value as declared in the XML document.
     * <p>
     * Resolution strategy:
     * <ul>
     *    <li>If the XSD path contains {@code "classpath:"}, the part following it is
     *       resolved from the classpath.</li>
     *   <li>If the XSD path contains {@code "resources/"}, the part following it is
     *       resolved from the classpath.</li>
     *   <li>Otherwise, the XSD is resolved as a file system path relative to the XML
     *       document location.</li>
     * </ul>
     * </p>
     *
     * @param document the parsed XML document; must not be {@code null}
     * @return the schema location (e.g. {@code "connector.xsd"} or a relative/absolute path)
     *         associated with the default namespace
     * @throws ReferenceTestException if
     *         <ul>
     *           <li>the document has no root element,</li>
     *           <li>no default namespace is declared,</li>
     *           <li>{@code xsi:schemaLocation} is missing or empty,</li>
     *           <li>or no schema location is defined for the default namespace.</li>
     *         </ul>
     */
    private static URL readConnectorXsd(Document document) throws ReferenceTestException {
        Objects.requireNonNull(document, "XML Document is null");

        Element root = document.getDocumentElement();
        Objects.requireNonNull(root, "No root element in the xml document");

        String defaultNamespace = root.getAttribute("xmlns");
        if (defaultNamespace.isBlank()) {
            throw new ReferenceTestException(
                    ReferenceError.NO_DEFAULT_NAMESPACE, "No default namespace is given (xmlns=\"...\")");
        }

        String schemaLocation = root.getAttributeNS(XSI_NAMESPACE, "schemaLocation");
        if (schemaLocation.isBlank()) {
            throw new ReferenceTestException(ReferenceError.NO_XSI_SCHEMA_LOCATION, "No xsi:schemaLocation is given");
        }

        Map<String, String> schemaMap = parseSchemaLocation(schemaLocation);
        String xsd = schemaMap.get(defaultNamespace);
        if (xsd == null || xsd.trim().isBlank()) {
            throw new ReferenceTestException(
                    ReferenceError.NO_NAMESPACE_SCHEMA_MAPPING,
                    "No schemaLocation mapping found for namespace: " + defaultNamespace);
        }

        // classpath resource
        if (xsd.startsWith("classpath:")) {
            String classpathXsd = xsd.substring("classpath:".length());
            URL url = Thread.currentThread().getContextClassLoader().getResource(classpathXsd);
            if (url == null) {
                throw new ReferenceTestException(
                        ReferenceError.MISSING_XSD_FILE_ON_CLASSPATH,
                        "Connector XSD not found on classpath: " + classpathXsd);
            }
            return url;
        }

        // exclude other protocols
        if (xsd.contains(":")) {
            throw new ReferenceTestException(ReferenceError.UNSUPPORTED_PROTOCOL, "The protocol is not supported");
        }

        // relative path - look for classpath first
        int index = xsd.indexOf("resources/");
        if (index >= 0) {
            xsd = xsd.substring(index + "resources/".length());
            URL url = Thread.currentThread().getContextClassLoader().getResource(xsd);
            if (url == null) {
                throw new ReferenceTestException(
                        ReferenceError.MISSING_XSD_FILE_ON_CLASSPATH_FROM_RELATIVE,
                        "Connector XSD not found in resources. Could not load from classpath: " + xsd);
            }
            return url;
        }

        // fallback: resolve with baseUri of the document
        return resolveRelativePath(document.getBaseURI(), xsd);
    }

    /**
     * Resolves an XSD location relative to an XML document base URI.
     * <p>
     * The base URI is expected to originate from {@link org.w3c.dom.Document#getBaseURI()}
     * and usually points to the XML file as a {@code file:} URI.
     * </p>
     *
     * @param baseUri base URI of the XML document
     * @param xsd     schema location from {@code xsi:schemaLocation}
     * @return URL of the resolved XSD
     * @throws ReferenceTestException if the XSD cannot be resolved
     */
    private static URL resolveRelativePath(String baseUri, String xsd) throws ReferenceTestException {
        // fallback load relative

        URI base = URI.create(baseUri);
        Path basePath;
        if ("file".equalsIgnoreCase(base.getScheme())) {
            basePath = Paths.get(base).toAbsolutePath().normalize();
        } else {
            throw new ReferenceTestException(ReferenceError.MISSING_XSD_FILE, "Base URI is not a file URI: " + baseUri);
        }

        Path baseDir = Files.isDirectory(basePath) ? basePath : basePath.getParent();

        if (baseDir == null) {
            throw new ReferenceTestException(
                    ReferenceError.MISSING_XSD_FILE, "Cannot resolve base directory from: " + baseUri);
        }

        Path resolved = baseDir.resolve(xsd).normalize().toAbsolutePath();

        try {
            if (Files.exists(resolved)) {
                return resolved.toUri().toURL();
            } else {
                throw new RuntimeException("Resolved file does not exists.");
            }
        } catch (Exception exception) {
            throw new ReferenceTestException(
                    ReferenceError.MISSING_XSD_FILE, "Connector XSD not found" + xsd, exception);
        }
    }

    /**
     * Parses the value of an {@code xsi:schemaLocation} attribute into a namespace-to-schema mapping.
     * <p>
     * The {@code xsi:schemaLocation} value is defined as a whitespace-separated list of
     * {@code <namespace URI> <schema location>} pairs. For example:
     *
     * <pre>
     * urn:example:connector connector.xsd
     * urn:example:common    common.xsd
     * </pre>
     *
     * This method converts such a value into a {@link Map} where the key is the namespace URI
     * and the value is the corresponding schema location.
     *
     * @param schemaLocation the raw {@code xsi:schemaLocation} attribute value; must not be {@code null}
     * @return a map of namespace URIs to schema locations (preserving declaration order)
     * @throws ReferenceTestException if the schema location string does not consist of valid
     *         namespace/schema pairs (i.e. an odd number of tokens)
     */
    private static Map<String, String> parseSchemaLocation(String schemaLocation) throws ReferenceTestException {

        if (schemaLocation.trim().isBlank()) {
            return Map.of();
        }

        String[] parts = schemaLocation.trim().split("\\s+");

        if (parts.length % 2 != 0) {
            throw new ReferenceTestException(
                    ReferenceError.INVALID_SCHEMA_PARSING,
                    "xsi:schemaLocation must contain pairs of '<namespace> <schema>' but had odd token count: "
                            + parts.length);
        }

        // schemaLocation needs to be pairs
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < parts.length; i += 2) {
            String ns = parts[i];
            String xsd = parts[i + 1];
            map.put(ns, xsd);
        }
        return map;
    }

    /**
     * Validates the connector input message against the connector-specific XSD.
     *
     * <p>The original XML message may contain elements and attributes from the
     * framework test namespace. Since these are not part of the connector schema,
     * they are removed prior to validation.</p>
     *
     * <p>Validation is intentionally limited to the connector-relevant message
     * fragment and does not include framework-specific configuration data.</p>
     *
     * @param document the original XML document containing framework and connector data
     * @param validator validator for validation the part of the document with given payload
     * @param payload <strong>input</strong>, if it is an input message, <strong>output</strong> for an output message
     * @throws ReferenceTestException if the validation for the given payload fails
     */
    private static void validateDocument(Validator validator, Document document, String payload)
            throws ReferenceTestException {

        Objects.requireNonNull(document, "XML Document is null");
        Objects.requireNonNull(payload, "No payload is given");

        // new Document to validate without test-namespace and output part
        // empty document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);

        // check if the output node is not missing
        Node outputNode = document.getElementsByTagName("output").item(0);
        if (outputNode == null) {
            throw new ReferenceTestException(ReferenceError.MISSING_OUTPUT, "No output tag is defined");
        }

        // root element
        Element root = document.getDocumentElement();

        DocumentBuilder builder;
        try {
            builder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException exception) {
            throw new RuntimeException("The ParserConfiguration fails", exception);
        }

        Objects.requireNonNull(builder, "The documentBuilder must not be null");

        Document outDoc = builder.newDocument();
        // copy root
        Element newRoot = (Element) outDoc.importNode(root, false);
        outDoc.appendChild(newRoot);

        Node node =
                document.getElementsByTagNameNS(root.getNamespaceURI(), payload).item(0);

        if (node != null) {
            Node newInput = outDoc.importNode(node, true);
            newRoot.appendChild(newInput);
            // eliminate test namespace elements (e.g. test:attachment) that are not part of the connector schema
            removeElementsByNamespace(newInput, TEST_NAMESPACE);
            // eliminate test namespace attributes
            if ("input".equals(payload)) {
                ((Element) newInput).removeAttributeNS(TEST_NAMESPACE, "interaction");
            } else if ("output".equals(payload)) {
                removeAttributesByNamespace(newInput, TEST_NAMESPACE);
            }

            try {

                validator.validate(new DOMSource(outDoc));
            } catch (SAXException | IOException exception) {
                throw new ReferenceTestException(
                        ReferenceError.INVALID_PAYLOAD, "Validation for " + payload + " failed", exception);
            }
        }
    }

    /**
     * parse a stream in a document tree.
     * @param xmlStream Byte array with xml data
     * @param xmlPath the path of the XML file, reported as the system id on parse errors
     * @return Document
     * @throws ReferenceTestException if the document is not well-formed
     */
    private static Document validateWellFormed(InputStream xmlStream, String xmlPath) throws ReferenceTestException {

        try {
            byte[] xmlBytes = xmlStream.readAllBytes();
            ByteArrayInputStream xmlByteStream = new ByteArrayInputStream(xmlBytes);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            InputSource inputSource = new InputSource(xmlByteStream);
            inputSource.setSystemId(xmlPath);
            return dbf.newDocumentBuilder().parse(inputSource);
        } catch (IOException | SAXException | ParserConfigurationException exception) {
            throw new ReferenceTestException(
                    ReferenceError.WELL_FORMED_READ,
                    "Error while reading the xml. Document may not well-formed.",
                    exception);
        }
    }

    /**
     * returns the element with the configuration part.
     * @param document DOM Document
     * @return Element witz the configuration from test namespace
     * @throws ReferenceTestException if no element is available
     */
    private static Element extractTestPart(Document document) throws ReferenceTestException {

        NodeList nodes = document.getElementsByTagNameNS(TEST_NAMESPACE, "configuration");

        Element result = (Element) nodes.item(0);
        if (result == null) {
            throw new ReferenceTestException(ReferenceError.TEST_EXTRACTION, "No configuration part in the message");
        }
        return result;
    }

    /**
     * validates the xml against the test.xsd provided by the framework.
     * @param document DOM tree
     * @throws ReferenceTestException if the test.xsd cannot be loaded from classpath
     */
    private static void validateTestNamespace(Document document) throws ReferenceTestException {
        Element element = extractTestPart(document);

        // need to create a new factory as the factory is stateful
        SchemaFactory testFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL url = XmlValidator.class.getResource("/" + TEST_XSD);
        if (url == null) {
            throw new RuntimeException("test.xsd is not available on classpath");
        }
        StreamSource stream = new StreamSource(url.toExternalForm());
        stream.setSystemId(url.toExternalForm());

        Schema testSchema;
        try {
            testSchema = testFactory.newSchema(stream);
        } catch (SAXException exception) {
            throw new RuntimeException("The test schema could not be read");
        }
        Validator testValidator = testSchema.newValidator();

        try {
            testValidator.validate(new DOMSource(element));
        } catch (SAXException | IOException exception) {
            throw new RuntimeException("The test configuration is not valid", exception);
        }
    }

    /**
     * Recursively removes all attributes belonging to the specified namespace
     * from the given DOM node and all of its descendant elements.
     *
     * <p>Only attributes are affected; elements themselves are never removed.
     * Namespace prefixes are ignored and resolution is performed solely based
     * on the namespace URI.</p>
     *
     * <p>This method mutates the provided DOM tree in place and is typically used
     * to strip framework-specific metadata prior to schema validation.</p>
     *
     * @param node the root node from which attribute removal starts
     * @param namespaceUri the namespace URI whose attributes should be removed
     */
    private static void removeAttributesByNamespace(Node node, String namespaceUri) {

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            NamedNodeMap attributes = element.getAttributes();
            for (int i = attributes.getLength() - 1; i >= 0; i--) {
                Node attr = attributes.item(i);
                if (namespaceUri.equals(attr.getNamespaceURI())) {
                    element.removeAttributeNode((Attr) attr);
                }
            }
        }

        // recursive for all elements
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            removeAttributesByNamespace(children.item(i), namespaceUri);
        }
    }

    /**
     * Recursively removes all child elements belonging to the specified namespace from the given DOM node.
     *
     * <p>This is used to strip framework-specific elements (e.g. {@code test:attachment}) that are not part
     * of the connector schema prior to validation.</p>
     *
     * @param node the root node from which element removal starts
     * @param namespaceUri the namespace URI whose elements should be removed
     */
    private static void removeElementsByNamespace(Node node, String namespaceUri) {

        // the child node next sibling is captured before a possible removal, so the live NodeList can be
        // iterated forward safely
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (namespaceUri.equals(child.getNamespaceURI())) {
                    node.removeChild(child);
                } else {
                    removeElementsByNamespace(child, namespaceUri);
                }
            }
            child = next;
        }
    }
}
