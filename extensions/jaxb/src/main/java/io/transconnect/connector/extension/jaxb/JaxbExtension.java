/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.extension.jaxb;

import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.extension.ConnectorExtension;
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
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Connector extension that provides JAXB marshalling and unmarshalling capabilities.
 * This extension allows connectors to easily convert between XML messages and Java objects
 * using JAXB with optional schema validation support.
 */
public class JaxbExtension implements ConnectorExtension {

    private static final Logger LOG = LoggerFactory.getLogger(JaxbExtension.class);

    /**
     * The JAXBContext instances used for marshalling and unmarshalling messages. Initialized on first use.
     */
    private final Map<Class<?>, JAXBContext> jaxbContexts = new HashMap<>();

    /**
     * The Marshaller instances used for marshalling the result message. Initialized on first use.
     */
    private final Map<Class<?>, Marshaller> marshallers = new HashMap<>();

    /**
     * The Unmarshaller instances used for unmarshalling the input message. Initialized on first use.
     */
    private final Map<Class<?>, Unmarshaller> unmarshallers = new HashMap<>();

    /**
     * Unmarshals an XML message into a Java object using JAXB.
     *
     * @param message the XML message to unmarshal
     * @param clazz the class type to unmarshal into
     * @param schemaResourcePath the resource path for the XML schema used for validation, may be null
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @param <T> the type to unmarshal into
     * @return the unmarshalled object
     * @throws TransconnectConnectorException if the message could not be unmarshalled
     */
    public <T> T unmarshalMessage(
            Message message, Class<T> clazz, String schemaResourcePath, String bindingResourcePath)
            throws TransconnectConnectorException {
        try {
            var unmarshaller = getInputUnmarshaller(clazz, schemaResourcePath, bindingResourcePath);
            T result = (T) unmarshaller.unmarshal(message.getXmlBody());
            LOG.debug("Successfully unmarshalled XML to {}", clazz.getSimpleName());

            return result;
        } catch (JAXBException | XMLStreamException e) {
            throw new TransconnectConnectorException(
                    "Failed to unmarshal XML to %s: %s".formatted(clazz.getSimpleName(), e.getMessage()), e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate result message from response object using marshalling.
     *
     * @param response the writable message to write the result into
     * @param resultClass the object to marshal into XML
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @param <T> the type of the object to marshal
     * @throws TransconnectConnectorException if the result message could not be generated
     */
    public <T> void marshalMessage(WritableMessage response, T resultClass, String bindingResourcePath)
            throws TransconnectConnectorException {
        try {
            try (OutputStream out = response.getBodyOutputStream()) {
                getResultMarshaller((Class<T>) resultClass.getClass(), bindingResourcePath)
                        .marshal(resultClass, out);
            }
        } catch (JAXBException | IOException e) {
            throw new TransconnectConnectorException("Error generating result message", e);
        }
    }

    /**
     * Marshal an object to an OutputStream using JAXB.
     *
     * @param resultClass the object to marshal into XML
     * @param outputStream the output stream to write the XML to
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @param <T> the type of the object to marshal
     * @throws TransconnectConnectorException if the object could not be marshalled
     */
    public <T> void marshal(T resultClass, OutputStream outputStream, String bindingResourcePath)
            throws TransconnectConnectorException {
        try {
            getResultMarshaller((Class<T>) resultClass.getClass(), bindingResourcePath)
                    .marshal(resultClass, outputStream);
        } catch (JAXBException e) {
            throw new TransconnectConnectorException("Error marshalling object to stream", e);
        }
    }

    /**
     * Clears all caches to release resources.
     * Call this method when shutting down the connector.
     */
    public void clearCaches() {
        var contextCount = jaxbContexts.size();
        var schemaCount = marshallers.size() + unmarshallers.size();

        jaxbContexts.clear();
        marshallers.clear();
        unmarshallers.clear();

        LOG.info("Cleared JAXB caches: {} contexts, {} schemas", contextCount, schemaCount);
    }

    /**
     * Returns an Unmarshaller for the specified input class, schema resource path, and binding resource path.
     * If the Unmarshaller does not exist, it creates a new one and caches it.
     *
     * @param <T> the type of the input class
     * @param inputClass the class to be unmarshalled
     * @param schemaResourcePath the resource path for the XML schema used for validation
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @return an Unmarshaller instance for the specified input class
     * @throws JAXBException if an error occurs while creating the Unmarshaller
     * @throws SAXException if an error occurs while loading the schema
     */
    private <T> Unmarshaller getInputUnmarshaller(
            Class<T> inputClass, String schemaResourcePath, String bindingResourcePath)
            throws JAXBException, SAXException {
        JAXBContext inputContext = jaxbContexts.get(inputClass);
        if (inputContext == null) {
            // create input context
            inputContext = getJaxbContext(inputClass, bindingResourcePath);
            jaxbContexts.put(inputClass, inputContext);
        }

        Unmarshaller inputUnmarshaller = unmarshallers.get(inputClass);
        if (inputUnmarshaller == null) {
            // create unmarshaller
            inputUnmarshaller = jaxbContexts.get(inputClass).createUnmarshaller();
            unmarshallers.put(inputClass, inputUnmarshaller);

            // Load schema for validation
            if (schemaResourcePath != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(
                        Thread.currentThread().getContextClassLoader().getResource(schemaResourcePath));
                inputUnmarshaller.setSchema(schema);
            }

            // Add error handling
            inputUnmarshaller.setEventHandler(event -> {
                LOG.error("Validation error: {}", event.getMessage());
                return false; // Return false to stop processing on the first error
            });
        }

        return inputUnmarshaller;
    }

    /**
     * Returns a Marshaller for the specified result class and binding resource path.
     * If the Marshaller does not exist, it creates a new one and caches it.
     *
     * @param <T> the type of the result class
     * @param resultClass the class to be marshalled
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @return a Marshaller instance for the specified result class
     * @throws JAXBException if an error occurs while creating the Marshaller
     */
    private <T> Marshaller getResultMarshaller(Class<T> resultClass, String bindingResourcePath) throws JAXBException {
        JAXBContext resultContext = jaxbContexts.get(resultClass);
        if (resultContext == null) {
            // create result context
            resultContext = getJaxbContext(resultClass, bindingResourcePath);
            jaxbContexts.put(resultClass, resultContext);
        }

        Marshaller resultMarshaller = marshallers.get(resultClass);
        if (resultMarshaller == null) {
            resultMarshaller = resultContext.createMarshaller();
            resultMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshallers.put(resultClass, resultMarshaller);
        }

        return resultMarshaller;
    }

    /**
     * Creates and returns a new JAXBContext instance configured with a specified class and custom properties.
     * This method uses the MOXy binding file located at "bindings/moxy-bindings.xml" for customization.
     *
     * @param <T> the type of the class to be included in the context
     * @param clazz the class for which the JAXBContext is to be created
     * @param bindingResourcePath the resource path for the MOXy binding file, may be null if not needed
     * @return a configured JAXBContext instance for the given class
     * @throws JAXBException if an error occurs while creating the JAXBContext
     */
    private <T> JAXBContext getJaxbContext(Class<T> clazz, String bindingResourcePath) throws JAXBException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Map<String, Object> properties = new HashMap<>();

        if (bindingResourcePath != null) {
            InputStream iStream = classLoader.getResourceAsStream(bindingResourcePath);
            if (iStream == null) {
                throw new JAXBException("Binding file not found: " + bindingResourcePath);
            }
            properties.put(JAXBContextProperties.OXM_METADATA_SOURCE, iStream);

            String packageName = clazz.getPackageName();
            LOG.debug(
                    "Creating package-based JAXBContext for package: {} with bindings: {}",
                    packageName,
                    bindingResourcePath);
            return JAXBContext.newInstance(packageName, classLoader, properties);
        }

        // Fall back to class-based context creation when no bindings are specified
        return JAXBContext.newInstance(new Class[] {clazz}, properties);
    }
}
