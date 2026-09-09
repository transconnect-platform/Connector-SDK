/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reference Parser to parse xml files for reference tests.
 */
public class ReferenceParser {

    /**
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(ReferenceParser.class);

    /**
     * configuration of the connector.
     *
     * @return configuration of the connector
     */
    @Getter
    private final Map<String, String> configuration;

    /**
     * attachments of the input message, mapping the attachment id to its binary content.
     *
     * @return attachments of the input message, mapping the attachment id to its binary content
     */
    @Getter
    private final Map<String, byte[]> inputAttachments;

    /**
     * expected attachments of the output message, mapping the attachment id to the referenced file path.
     * The file content is read lazily during the comparison to save memory.
     *
     * @return expected attachments of the output message, mapping the attachment id to the referenced file path
     */
    @Getter
    private final Map<String, String> outputAttachments;

    /**
     * the interaction for the test.
     *
     * @return the interaction for the test
     */
    @Getter
    private URI interaction;

    /**
     * true, if the input part is missing, otherwise false.
     *
     * @return true, if the input part is missing, otherwise false
     */
    @Getter
    private boolean consumer = false;

    /**
     * XML Document.
     */
    private Document document;

    /**
     * Reference Parser to parse a reference file for Consumer- or Producer Connectors.
     */
    public ReferenceParser() {
        configuration = new HashMap<>();
        inputAttachments = new HashMap<>();
        outputAttachments = new HashMap<>();
    }

    /**
     * extracts the configuration, and the interaction from the given document.
     *
     * @param xml the reference file content to parse
     * @param xmlPath the path of the reference file, used to resolve attachment files
     * @throws ReferenceTestException if the extracting fails
     */
    public void parse(InputStream xml, String xmlPath) throws ReferenceTestException {
        this.document = XmlValidator.validateXmlSchema(xml, xmlPath);
        parseConfiguration();
        parseInteraction();
        parseAttachments("input", xmlPath);
        parseAttachments("output", xmlPath);
    }

    /**
     * reads the interaction in the document.
     * @throws ReferenceTestException if the interaction is null
     */
    private void parseInteraction() throws ReferenceTestException {

        Element root = document.getDocumentElement();
        String namespaceUri = root.getNamespaceURI();
        NodeList inputs = root.getElementsByTagNameNS(namespaceUri, "input");

        if (inputs.getLength() > 0) {

            // input tag is available, it is a consumer
            consumer = true;

            Element inputElement = (Element) inputs.item(0);
            String interactionValue = inputElement
                    .getAttributeNS(XmlValidator.TEST_NAMESPACE, "interaction")
                    .trim();

            if (!interactionValue.isEmpty()) {
                interaction = URI.create(interactionValue);
            }
        } else {
            consumer = false;
        }

        // interaction is necessary
        if (consumer && interaction == null) {
            throw new ReferenceTestException("Attribute test:interaction in input element is not found or empty");
        }
    }

    /**
     * reads the {@code <test:attachment id="..." file="..."/>} declarations of the given payload element.
     * The files are resolved relative to the location of the reference xml. For input attachments the content is
     * loaded eagerly (it has to be attached to the input message); for output attachments only the file reference
     * is stored and the content is read lazily during the comparison to save memory.
     * @param payloadTag the payload element to read the attachments from ("input" or "output")
     * @param xmlPath the path of the reference xml (a file or its containing directory)
     * @throws ReferenceTestException if an attachment declaration is invalid or an input file cannot be read
     */
    private void parseAttachments(String payloadTag, String xmlPath) throws ReferenceTestException {

        Element root = document.getDocumentElement();
        NodeList payloads = root.getElementsByTagNameNS(root.getNamespaceURI(), payloadTag);
        if (payloads.getLength() == 0) {
            return;
        }

        Element payloadElement = (Element) payloads.item(0);
        NodeList attachmentNodes = payloadElement.getElementsByTagNameNS(XmlValidator.TEST_NAMESPACE, "attachment");
        if (attachmentNodes.getLength() == 0) {
            return;
        }

        Path baseDir = resolveBaseDirectory(xmlPath);

        for (int i = 0; i < attachmentNodes.getLength(); i++) {
            Element attachment = (Element) attachmentNodes.item(i);
            String id = attachment.getAttribute("id").trim();
            String file = attachment.getAttribute("file").trim();

            if (id.isEmpty() || file.isEmpty()) {
                throw new ReferenceTestException("test:attachment requires a non-empty id and file attribute");
            }

            Path attachmentFile = baseDir.resolve(file);
            if ("input".equals(payloadTag)) {
                try {
                    inputAttachments.put(id, Files.readAllBytes(attachmentFile));
                } catch (IOException exception) {
                    throw new ReferenceTestException("Could not read attachment file: " + attachmentFile, exception);
                }
            } else if ("output".equals(payloadTag)) {
                outputAttachments.put(id, attachmentFile.toString());
            }
        }
    }

    /**
     * resolves the directory that contains the reference xml and its attachments.
     * @param xmlPath the reference xml path, either the xml file itself or its containing directory
     * @return the directory used to resolve attachment files
     */
    private static Path resolveBaseDirectory(String xmlPath) {
        Path path = Paths.get(xmlPath);
        if (Files.isDirectory(path)) {
            return path;
        }
        Path parent = path.getParent();
        return parent != null ? parent : path;
    }

    /**
     * parse the configuration part in a property map.
     */
    private void parseConfiguration() throws ReferenceTestException {
        Objects.requireNonNull(document, "No XML Document available");

        Element root = document.getDocumentElement();

        NodeList configList = root.getElementsByTagNameNS(XmlValidator.TEST_NAMESPACE, "configuration");

        if (configList.getLength() == 0) {
            logger.debug("No configuration fields are given in the reference test xml");
        }

        if (configList.getLength() > 0) {
            Element config = (Element) configList.item(0);

            // all <test:property>
            NodeList propertyList = config.getElementsByTagNameNS(XmlValidator.TEST_NAMESPACE, "property");

            if (propertyList.getLength() == 0) {
                throw new ReferenceTestException("Property test namespace is not given");
            }

            for (int i = 0; i < propertyList.getLength(); i++) {
                Element property = (Element) propertyList.item(i);

                String id = property.getAttribute("id");
                String value = property.getTextContent().trim();

                configuration.put(id, value);
            }
        }
    }

    /**
     * returns the test message for the connector.
     * for connectors which use jaxb extension you need to filter the test namespace
     * @param input true, if the input message should be returned, otherwise false
     * @return xml message
     */
    public String getMessage(boolean input) {

        Objects.requireNonNull(this.document, "The document was not parsed yet");

        String type = input ? "input" : "output";

        Element root = this.document.getDocumentElement();

        // empty document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException exception) {
            throw new RuntimeException("The ParserConfiguration fails", exception);
        }

        Objects.requireNonNull(builder, "The documentBuilder must not be null");
        Document outDoc = builder.newDocument();

        // copy root
        Element newRoot = (Element) outDoc.importNode(root, false);
        outDoc.appendChild(newRoot);

        // the xsi:schemaLocation is only needed for the validation of the reference test and must not be
        // forwarded to the connector within the message that is sent to it
        if (input) {
            newRoot.removeAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
        }

        // get child selective
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            // don't add test configuration
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && XmlValidator.TEST_NAMESPACE.equals(child.getNamespaceURI())) {
                continue;
            }

            // test elements (e.g. test:attachment) are metadata and must never be part of the payload,
            // neither in the message sent to the connector nor in the expected output that is compared
            Node imported = outDoc.importNode(child, true);
            removeElementsByNamespaceDeep(imported, XmlValidator.TEST_NAMESPACE);

            // for the message sent to the connector also drop test attributes (e.g. test:interaction)
            if (input) {
                removeAttributesByNamespaceDeep(imported, XmlValidator.TEST_NAMESPACE);
            }

            if (type.equals(imported.getLocalName())) {
                newRoot.appendChild(imported);
            }
        }

        return documentToString(outDoc);
    }

    /**
     * remove attributes belonging to the test namespace.
     * necessary for the jaxb extension
     * @param node the actual Node
     * @param namespaceUri the Namespace
     */
    private static void removeAttributesByNamespaceDeep(Node node, String namespaceUri) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element el = (Element) node;

            NamedNodeMap attributesMap = el.getAttributes();
            for (int i = attributesMap.getLength() - 1; i >= 0; i--) {
                Attr attribute = (Attr) attributesMap.item(i);
                if (namespaceUri.equals(attribute.getNamespaceURI())) {
                    el.removeAttributeNode(attribute);
                }
            }
        }

        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            removeAttributesByNamespaceDeep(child, namespaceUri);
            child = next;
        }
    }

    /**
     * remove child elements belonging to the given namespace (e.g. test:attachment).
     * used to strip test elements from the message that is sent to the connector
     * @param node the actual Node
     * @param namespaceUri the Namespace
     */
    private static void removeElementsByNamespaceDeep(Node node, String namespaceUri) {
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.ELEMENT_NODE && namespaceUri.equals(child.getNamespaceURI())) {
                node.removeChild(child);
            } else {
                removeElementsByNamespaceDeep(child, namespaceUri);
            }
            child = next;
        }
    }

    /**
     * transform a Document to String.
     * @param doc DOM Document
     * @return xml as String
     */
    private static String documentToString(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException exception) {
            throw new RuntimeException("DOM is invalid", exception);
        }
    }
}
