/*
 * (c) Copyright 2026 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.api.extension;

import io.transconnect.connector.api.IConnector;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

/**
 * Manages the loading and application of connector extensions for a specific connector type.
 * <p>This manager is responsible for discovering, loading, and applying extensions to connectors
 * based on the {@link Connector} annotation and {@link Extension} annotations on the connector class.
 * It uses Java's ServiceLoader mechanism to find and instantiate extension implementations.</p>
 * @see ConnectorExtension
 * @see Connector
 * @see Extension
 * @see DescriptorExtension
 */
public class ExtensionManager {

    /**
     * The ServiceLoader used to discover connector extensions.
     */
    private final ServiceLoader<ConnectorExtension> extensionLoader;

    /**
     * The connector class for which extensions are managed.
     */
    private final Class<? extends IConnector<?>> connectorClass;

    /**
     * List of loaded extensions.
     */
    private final List<ConnectorExtension> extensions = new ArrayList<>();

    /**
     * Creates a new ExtensionManager for the specified connector class.
     *
     * @param clazz the connector class for which extensions should be managed
     */
    public ExtensionManager(Class<? extends IConnector<?>> clazz) {
        this.extensionLoader = ServiceLoader.load(ConnectorExtension.class, clazz.getClassLoader());
        this.connectorClass = clazz;
    }

    /**
     * Retrieves all extensions for this connector.
     *
     * <p>This method will load extensions if they haven't been loaded yet. It reads the
     * {@link Connector} annotation on the connector class to determine which extensions
     * should be applied, then uses the ServiceLoader to find and instantiate them.</p>
     *
     * @return a list of loaded extensions
     * @throws TransconnectConnectorException if an extension cannot be found or loaded
     */
    public List<ConnectorExtension> getExtensions() throws TransconnectConnectorException {
        if (extensions.isEmpty()) {
            Connector connectorAnnotation = connectorClass.getAnnotation(Connector.class);
            if (connectorAnnotation != null) {
                Extension[] extensionAnnotations = connectorAnnotation.extensions();
                this.extensionLoader.reload();
                for (Extension extension : extensionAnnotations) {
                    var extensionClass = extension.value();
                    var extensionInstance = extensionLoader.stream()
                            .filter(ep -> ep.type().isAssignableFrom(extensionClass))
                            .findFirst()
                            .map(Provider::get)
                            .orElseThrow(() -> new TransconnectConnectorException("Unknown extension " + extensionClass
                                    + ". Extension could not be found via Service Loader in classpath."));
                    extensions.add(extensionInstance);
                }
            }
        }
        return extensions;
    }

    /**
     * Retrieves all extensions that are instances of the specified type.
     *
     * @param extensionClass the type of extensions to retrieve
     * @param <T> the type of extensions to retrieve
     * @return a list of extensions of the specified type
     * @throws TransconnectConnectorException if there is an error retrieving extensions
     */
    public <T extends ConnectorExtension> List<T> getExtensionsByType(Class<T> extensionClass)
            throws TransconnectConnectorException {
        return getExtensions().stream()
                .filter(extensionClass::isInstance)
                .map(extensionClass::cast)
                .toList();
    }

    /**
     * Applies all descriptor extensions to the specified producer connector descriptor.
     *
     * <p>Descriptor extensions modify the connector description.</p>
     *
     * @param connectorDesc the producer connector descriptor to be modified
     * @return the modified producer connector descriptor
     * @throws TransconnectConnectorException if there is an error applying the extensions
     */
    public ProducerConnectorDescriptor applyExtensions(ProducerConnectorDescriptor connectorDesc)
            throws TransconnectConnectorException {
        var desc = connectorDesc;
        var descriptorExtensions = getExtensionsByType(DescriptorExtension.class);
        for (DescriptorExtension descriptorExtension : descriptorExtensions) {
            desc = descriptorExtension.modifyDescription(desc);
        }
        return desc;
    }

    /**
     * Applies all descriptor extensions to the specified consumer connector descriptor.
     *
     * <p>Descriptor extensions modify the connector description.</p>
     *
     * @param connectorDesc the consumer connector descriptor to be modified
     * @return the modified consumer connector descriptor
     * @throws TransconnectConnectorException if there is an error applying the extensions
     */
    public ConsumerConnectorDescriptor applyExtensions(ConsumerConnectorDescriptor connectorDesc)
            throws TransconnectConnectorException {
        var desc = connectorDesc;
        var descriptorExtensions = getExtensionsByType(DescriptorExtension.class);
        for (DescriptorExtension descriptorExtension : descriptorExtensions) {
            desc = descriptorExtension.modifyDescription(desc);
        }
        return desc;
    }
}
