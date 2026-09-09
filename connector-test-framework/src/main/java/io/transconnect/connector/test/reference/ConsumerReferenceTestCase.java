/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.MockContext;
import io.transconnect.connector.MockMessage;
import io.transconnect.connector.MockWritableMessage;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.consumer.ConsumerConnector;
import io.transconnect.connector.api.consumer.ConsumerConnectorDescriptor;
import io.transconnect.connector.api.extension.ExtensionManager;
import io.transconnect.connector.api.message.Message;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;

/**
 * Testcase for consumer connector.
 */
public class ConsumerReferenceTestCase extends AbstractReferenceTestCase {

    /**
     * input message.
     *
     * @return input message
     */
    @Getter
    private final Message input;

    /**
     * consumer connector with given configuration.
     *
     * @return consumer connector with given configuration
     */
    @Getter
    private ConsumerConnector connector;

    /**
     * Configuration for the connector.
     *
     * @return configuration for the connector
     */
    @Getter
    private Configuration configuration;

    /**
     * configuration Map which will be mapped to the Connector Configuration.
     */
    private final Map<String, String> configMap;

    /**
     * the executable interaction.
     *
     * @return the executable interaction
     */
    @Getter
    private final URI interaction;

    /**
     * flag, to ignore namespaces while comparing the results.
     * true, if namespace should be ignored, otherwise false.
     */
    private boolean ignoreNamespaces = false;

    /**
     * Connector descriptor.
     */
    private ConsumerConnectorDescriptor connectorDescriptor;

    /**
     * Constructs a new ConsumerReferenceTestCase instance.
     *
     * @param refFile reference file for the test case
     * @param parser the ReferenceParser which parsed the document
     */
    ConsumerReferenceTestCase(File refFile, ReferenceParser parser) {
        this(refFile, parser, false, false);
    }

    /**
     * Constructs a new ConsumerReferenceTestCase instance.
     * @param refFile reference file for the test case
     * @param parser the ReferenceParser which parsed the document
     * @param ignoreNamespaces true, if the namespaces should be ignored while comparison, otherwise false
     * @param compareAttachments true, if the output attachments should be compared, otherwise false
     */
    ConsumerReferenceTestCase(
            File refFile, ReferenceParser parser, boolean ignoreNamespaces, boolean compareAttachments) {
        super(
                refFile,
                parser.getMessage(false),
                XmlValidator.TEST_NAMESPACE,
                parser.getOutputAttachments(),
                compareAttachments);
        configMap = parser.getConfiguration();
        input = createInputMessage(parser);
        this.interaction = parser.getInteraction();
        this.ignoreNamespaces = ignoreNamespaces;
    }

    /**
     * Builds the input message and attaches the input attachments provided by the parser.
     *
     * @param parser the parser holding the input body and the input attachments (id to bytes)
     * @return the input message with its attachments
     */
    private static MockMessage createInputMessage(ReferenceParser parser) {

        MockMessage message = new MockMessage(parser.getMessage(true));

        Map<String, byte[]> attachments = parser.getInputAttachments();
        if (attachments != null) {
            attachments.forEach(message::addAttachment);
        }

        return message;
    }

    /**
     * Loads the consumer connector with a given provider.
     *
     * @param provider connector provider
     * @param consumerClass the consumer connector class to load, or the marker class to load via ServiceLoader
     * @throws ReferenceTestException if loading fails
     */
    public void loadConnector(ConnectorProvider provider, Class<? extends ConsumerConnector> consumerClass)
            throws ReferenceTestException {

        Objects.requireNonNull(provider, "The connector provider must not be null");

        Optional<ConsumerConnector> consumerOptional = provider.loadConsumer(consumerClass);
        connector = consumerOptional.orElseThrow(() -> new ReferenceTestException("Could not load consumer connector"));
        ExtensionManager extensionManager = new ExtensionManager(connector.getClass());
        try {
            extensionManager.getExtensions();
            connectorDescriptor = extensionManager.applyExtensions(connector.getDescription());
        } catch (TransconnectConnectorException exception) {
            throw new ReferenceTestException("Could not load extension for ConsumerConnector", exception);
        }
        configuration = createConfiguration(configMap);
    }

    /**
     * Creates the configuration for the loaded connector.
     *
     * @param config configuration map
     * @return the configuration for the loaded connector
     * @throws ReferenceTestException if the connector is not loaded
     */
    private Configuration createConfiguration(Map<String, String> config) throws ReferenceTestException {

        if (connector == null) {
            throw new ReferenceTestException("No consumer connector is provided");
        }

        if (config == null) {
            throw new ReferenceTestException("The configuration map is null");
        }

        if (connectorDescriptor == null) {
            throw new ReferenceTestException("The connector descriptor was not provided");
        }
        return mapConnectorProperties(connectorDescriptor.getProperties(), config);
    }

    /**
     * Runs the test with the given data. Compares the output and expected output, writes a diff file.
     *
     * @return returned message
     * @throws ReferenceTestException connector exception is thrown
     */
    @Override
    public Message run() throws ReferenceTestException {

        if (connector == null) {
            throw new ReferenceTestException("Cannot run the reference test: connector not loaded.");
        }

        if (configuration == null) {
            throw new ReferenceTestException("Cannot run the reference test: configuration not loaded.");
        }

        ExtensionManager extensionManager = new ExtensionManager(connector.getClass());
        MockWritableMessage writeableMessage = new MockWritableMessage();

        MockContext context;
        try {
            context = new MockContext(configuration, extensionManager.getExtensions());
        } catch (TransconnectConnectorException exception) {
            throw new ReferenceTestException("Could not create MockContext for the configuration", exception);
        }

        try (ConsumerConnection connection = connector.createConnection(context)) {
            connection.connect();
            if (connection.isConnected()) {
                connection.execute(interaction, input, writeableMessage);

                // compare result with expected result
                compareResults(writeableMessage);
            }
        } catch (Exception e) {
            throw new ReferenceTestException("Test failed. " + e.getMessage(), e);
        }

        return writeableMessage;
    }

    /**
     * namespace should be ignored while comparison.
     * @return false
     */
    @Override
    protected boolean ignoreCompareNamespace() {
        return ignoreNamespaces;
    }
}
