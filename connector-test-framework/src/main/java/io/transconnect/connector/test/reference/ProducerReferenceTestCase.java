/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import io.transconnect.connector.MockContext;
import io.transconnect.connector.MockWritableMessage;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.extension.ExtensionManager;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.MessageFactory;
import io.transconnect.connector.api.message.WritableMessage;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnector;
import io.transconnect.connector.api.producer.ProducerConnectorDescriptor;
import io.transconnect.connector.api.producer.ProducerConnectorListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testcase for producer connector.
 */
public class ProducerReferenceTestCase extends AbstractReferenceTestCase
        implements ProducerConnectorListener, MessageFactory<WritableMessage>, AutoCloseable {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ProducerReferenceTestCase.class);

    /**
     * Producer connector with given configuration.
     *
     * @return producer connector with given configuration
     */
    @Getter
    private ProducerConnector connector;

    /**
     * Active Connection.
     *
     * @return active Connection
     */
    @Getter
    private ProducerConnection activeConnection;

    /**
     * Property map from parser.
     */
    private final Map<String, String> configMap;

    /**
     * configuration of the connector.
     *
     * @return configuration of the connector
     */
    @Getter
    private Configuration configuration;

    /**
     * Result Message.
     *
     * @return result Message
     */
    @Getter
    private Message result;

    /**
     * collection of errors.
     */
    private final List<Throwable> errors;

    /**
     * flag, to ignore namespaces while comparing the results.
     * true, if namespace should be ignored, otherwise false.
     */
    private boolean ignoreNamespaces = true;

    /**
     * Connector descriptor.
     */
    private ProducerConnectorDescriptor connectorDescriptor;

    /**
     * Constructs a new ProducerTest Case instance with given parameters.
     *
     * @param refFile Reference file for the test case.
     * @param parser ReferenceParser with already parsed document
     */
    public ProducerReferenceTestCase(File refFile, ReferenceParser parser) {
        this(refFile, parser, true, false);
    }

    /**
     * Constructs a new ProducerTest Case instance with given parameters.
     *
     * @param refFile Reference file for the test case.
     * @param parser ReferenceParser with already parsed document
     * @param ignoreNamespaces true, if the namespaces should be ignored while comparison, otherwise false
     * @param compareAttachments true, if the output attachments should be compared, otherwise false
     */
    public ProducerReferenceTestCase(
            File refFile, ReferenceParser parser, boolean ignoreNamespaces, boolean compareAttachments) {
        super(
                refFile,
                parser.getMessage(false),
                XmlValidator.TEST_NAMESPACE,
                parser.getOutputAttachments(),
                compareAttachments);
        errors = new ArrayList<>();
        configMap = parser.getConfiguration();
        this.ignoreNamespaces = ignoreNamespaces;
    }

    /**
     * Loads the connector and creates an active connection for the test case.
     *
     * @param connectorProvider The ConnectorProvider which loads the connector via ServiceLoader.
     * @param producerClass the producer connector class to load, or the marker class to load via ServiceLoader
     * @throws ReferenceTestException If loading or connecting to the connector fails.
     */
    public void loadConnector(ConnectorProvider connectorProvider, Class<? extends ProducerConnector> producerClass)
            throws ReferenceTestException {

        Objects.requireNonNull(connectorProvider, "No given ConnectorProvider");
        Objects.requireNonNull(configMap, "No given ConfigurationMap");

        // load the connector
        connector = connectorProvider
                .loadProducer(producerClass)
                .orElseThrow(() -> new ReferenceTestException("Could not load producer connector"));

        // load the extensions
        ExtensionManager extensionManager = new ExtensionManager(connector.getClass());
        try {
            extensionManager.getExtensions();
            connectorDescriptor = extensionManager.applyExtensions(connector.getDescription());
        } catch (TransconnectConnectorException exception) {
            throw new ReferenceTestException("Could not load extensions from ProducerConnector", exception);
        }

        // create the configuration of the connector
        configuration = createConfiguration(configMap);

        try {

            MockContext context = new MockContext(configuration, extensionManager.getExtensions());

            // create an active connection for the reference test
            activeConnection = connector.createConnection(context);

            if (activeConnection == null) {
                throw new ReferenceTestException("No connection was created");
            }

            // connect
            activeConnection.connect(this, this);

        } catch (Exception exception) {
            throw new ReferenceTestException("Could not create connection from configuration.", exception);
        }
    }

    /**
     * Creates the configuration of the connector using the provided map.
     *
     * @param config Configuration Map out of the reference file.
     * @return The created Configuration object
     * @throws ReferenceTestException If mapping fails.
     */
    private Configuration createConfiguration(Map<String, String> config) throws ReferenceTestException {

        Objects.requireNonNull(config, "No given ConfigurationMap");

        if (connector == null) {
            throw new ReferenceTestException("No producer connector is provided");
        }

        if (connectorDescriptor == null) {
            throw new ReferenceTestException("No connector descriptor provided");
        }
        return mapConnectorProperties(connectorDescriptor.getProperties(), config);
    }

    /**
     * Runs the comparison with expected output, or throws an exception if errors occur.
     *
     * @return The created message after comparison or null, if no result is available
     * @throws ReferenceTestException If an error occurs during the test.
     */
    @Override
    public Message run() throws ReferenceTestException {

        if (!errors.isEmpty()) {
            throw new ReferenceTestException("Errors occurs while reference test.", errors.get(0));
        }

        if (result != null) {
            return compareResults(result);
        }
        logger.warn("No message was created");
        return null;
    }

    /**
     * namespace should ignored while comparison.
     * @return true
     */
    @Override
    protected boolean ignoreCompareNamespace() {
        return this.ignoreNamespaces;
    }

    /**
     * Callback method when a message is produced by the connector.
     *
     * @param message The produced message.
     * @return A Future representing the result of the operation, which we don't use here so it returns null
     */
    @Override
    public Future<Message> onMessage(Message message) {
        result = message;
        // for testing purpose not needed
        return null;
    }

    /**
     * Callback method when a message is produced without waiting for the future result.
     *
     * @param message The produced message.
     */
    @Override
    public void onMessageWithoutResult(Message message) {
        result = message;
    }

    /**
     * Callback method when an error occurs during connector operations.
     *
     * @param throwable The Throwable representing the error.
     */
    @Override
    public void onError(Throwable throwable) {
        // Log error
        errors.add(throwable);
    }

    /**
     * Callback method when the connector is connected successfully.
     */
    @Override
    public void onConnect() {
        // log connection established
        logger.debug("Connection established");
    }

    /**
     * Callback method when the connector is disconnected.
     */
    @Override
    public void onDisconnect() {
        // log connection abort
        logger.debug("Connection was closed");
    }

    /**
     * Creates a writable message using the MockWritableMessage.
     *
     * @return A new instance of WritableMessage
     */
    @Override
    public WritableMessage createMessage() {
        return new MockWritableMessage();
    }

    /**
     * Closes the connection opened by this test case, if it is still connected.
     *
     * @throws Exception if the connection could not be closed
     */
    @Override
    public void close() throws Exception {
        if (activeConnection != null && activeConnection.isConnected()) {
            try {
                activeConnection.close();
            } catch (Exception exception) {
                throw new ReferenceTestException("Could not close connector", exception);
            }
        }
    }
}
