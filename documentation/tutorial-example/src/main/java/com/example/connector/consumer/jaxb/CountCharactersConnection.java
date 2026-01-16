/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.consumer.jaxb;

import com.example.connector.countcharacters.execute.in.ROOT;
import com.example.connector.util.XmlUtils;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer connection implementation that counts characters in an XML input.
 * <p>
 * This connection processes incoming XML messages containing a ROOT element with character data
 * and counts the number of letters and, optionally, whitespace characters.
 * </p>
 */
public final class CountCharactersConnection implements ConsumerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(CountCharactersConnection.class);
    private static final String INTERACTION_COUNT_CHARS_URI = "countCharacters";

    private final Configuration configuration;
    private final XMLOutputFactory outputFactory;

    /**
     * Creates a new connection with the specified configuration.
     *
     * @param configuration the connector configuration
     */
    public CountCharactersConnection(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
        this.outputFactory = XMLOutputFactory.newFactory();
    }

    @Override
    public void connect() {
        // No connection setup needed.
    }

    /**
     * Executes the character counting operation based on the interaction ID.
     *
     * @param interactionId the URI identifying the interaction
     * @param message the input message containing the text to analyze
     * @param response the writable message to contain the response
     * @throws TransconnectConnectorException if an error occurs during execution
     */
    @Override
    public void execute(URI interactionId, Message message, WritableMessage response)
            throws TransconnectConnectorException {

        try {
            if (INTERACTION_COUNT_CHARS_URI.equalsIgnoreCase(interactionId.toString())) {
                var countWhitespaces =
                        configuration.getBoolean("countWhitespaces").orElse(Boolean.FALSE);
                executeCountCharacters(message, response, countWhitespaces);
            } else {
                throw new TransconnectConnectorException("Unknown Interaction: '%s'".formatted(interactionId));
            }
        } catch (XMLStreamException | IOException e) {
            throw new TransconnectConnectorException("Error processing message", e);
        }
    }

    /**
     * Extracts the text from the ROOT element using an XMLEventReader,
     * counts characters, and writes the results.
     *
     * @param message the message containing the text to analyze
     * @param response the response to write results to
     * @param countWhitespaces whether to include whitespace in the count
     * @throws XMLStreamException if an XML parsing error occurs
     * @throws IOException if an I/O error occurs
     */
    private void executeCountCharacters(Message message, WritableMessage response, boolean countWhitespaces)
            throws XMLStreamException, IOException, TransconnectConnectorException {
        // tag::unmarshal[]
        var root = XmlUtils.unmarshalMessage(message, ROOT.class, "/com/example/connector/consumer/execute_in.xsd");
        var content = root.getCONTENT();
        // end::unmarshal[]

        LOG.debug("Processing text: {}", content);
        // tag::marshal[]
        var resultRoot = new com.example.connector.countcharacters.execute.out.ROOT();
        resultRoot.setCOUNT(countCharacters(content, countWhitespaces));
        XmlUtils.marshalMessage(response, resultRoot);
        // end::marshal[]
    }

    /**
     * Counts the characters in a text based on the counting rules.
     *
     * @param text the text to analyze
     * @param countWhitespaces whether to include whitespace in the count
     * @return the number of characters according to the counting rules
     */
    private long countCharacters(String text, boolean countWhitespaces) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        if (countWhitespaces) {
            return text.length();
        } else {
            return text.chars().filter(Character::isLetter).count();
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void close() {
        // No resources to release.
    }
}
