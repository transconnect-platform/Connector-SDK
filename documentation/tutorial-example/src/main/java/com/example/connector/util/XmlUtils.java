package com.example.connector.util;

import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public final class XmlUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XmlUtils.class);

    // Cache JAXBContext instances as they are expensive to create
    private static final Map<String, JAXBContext> jaxbContextCache = new ConcurrentHashMap<>();

    // Configure SchemaFactory once
    private static final SchemaFactory schemaFactory;

    static {
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Apply security constraints to XML factories
        try {
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (SAXException e) {
            LOG.warn("Could not set security properties on SchemaFactory", e);
        }
    }

    // Private constructor to prevent instantiation
    private XmlUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Unmarshals an XML message into a Java object using JAXB with schema validation.
     *
     * @param <T> the type of the resulting object
     * @param message the message containing XML to unmarshal
     * @param clazz the class of the resulting object
     * @param schemaPath the classpath location of the XML schema for validation
     * @return the unmarshalled object
     * @throws TransconnectConnectorException if unmarshalling fails
     */
    public static <T> T unmarshalMessage(Message message, Class<T> clazz, String schemaPath)
            throws TransconnectConnectorException {
        try {
            JAXBContext context = getJaxbContext(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Load schema for validation if schema path is provided
            if (schemaPath != null) {
                Schema schema = schemaFactory.newSchema(XmlUtils.class.getResource(schemaPath));
                unmarshaller.setSchema(schema);

                // Add error handling
                unmarshaller.setEventHandler(event -> {
                    LOG.error("Validation error: {}", event.getMessage());
                    return false; // Return false to stop processing on the first error
                });
            }

            // Deserialize XML to Java object
            @SuppressWarnings("unchecked")
            T result = (T) unmarshaller.unmarshal(message.getXmlBody());
            return result;
        } catch (JAXBException | SAXException | XMLStreamException e) {
            LOG.error("XML processing error during unmarshalling", e);
            throw new TransconnectConnectorException("Error processing XML message", e);
        }
    }

    /**
     * Marshals a Java object into XML and writes it to the provided writable message.
     *
     * @param <T> the type of the object to marshal
     * @param response the writable message to write to
     * @param responseRoot the object to marshal
     * @throws TransconnectConnectorException if marshalling fails
     */
    public static <T> void marshalMessage(WritableMessage response, T responseRoot)
            throws TransconnectConnectorException {
        try {
            JAXBContext context = getJaxbContext(responseRoot.getClass());

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Marshal to result message
            try (OutputStream out = response.getBodyOutputStream()) {
                marshaller.marshal(responseRoot, out);
            }
        } catch (JAXBException | IOException e) {
            throw new TransconnectConnectorException("Error generating result message", e);
        }
    }

    /**
     * Creates and returns a JAXBContext instance configured with a specified class and custom properties.
     * This method uses the MOXy binding file located at "bindings/moxy-bindings.xml" for customization.
     * JAXBContext instances are cached for better performance.
     *
     * @param <T> the type of the class to be included in the context
     * @param clazz the class for which the JAXBContext is to be created
     * @return a configured JAXBContext instance for the given class
     * @throws JAXBException if an error occurs while creating the JAXBContext
     */
    private static <T> JAXBContext getJaxbContext(Class<T> clazz) throws JAXBException {
        String packageName = clazz.getPackageName();

        // Check the cache first
        return jaxbContextCache.computeIfAbsent(packageName, k -> {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

                // Load additional moxy configuration
                try (InputStream iStream = classLoader.getResourceAsStream("bindings/moxy-bindings.xml")) {
                    if (iStream == null) {
                        LOG.warn("Could not find moxy-bindings.xml, using default JAXB configuration");
                        return JAXBContext.newInstance(packageName, classLoader);
                    }

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);
                    return JAXBContext.newInstance(packageName, classLoader, properties);
                }
            } catch (JAXBException | IOException e) {
                LOG.error("Failed to create JAXBContext for package: {}", packageName, e);
                throw new RuntimeException("Failed to create JAXBContext", e);
            }
        });
    }

    /**
     * Clears the JAXBContext cache.
     * Call this method when shutting down to release resources.
     */
    public static void clearCache() {
        jaxbContextCache.clear();
        LOG.debug("JAXBContext cache cleared");
    }
}
