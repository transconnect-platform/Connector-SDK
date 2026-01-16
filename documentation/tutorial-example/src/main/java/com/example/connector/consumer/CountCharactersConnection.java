package com.example.connector.consumer;

import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

/**
 * A consumer connection implementation that counts characters in an XML input.
 * <p>
 * This connection processes incoming XML messages containing a ROOT element with character data
 * and counts the number of letters and, optionally, whitespace characters.
 * </p>
 */
public final class CountCharactersConnection implements ConsumerConnection {

    // tag::constructor[]
    private static final String INTERACTION_COUNT_CHARS_URI = "countCharacters";
    private final Configuration configuration;

    public CountCharactersConnection(Context ctx) {
        this.configuration = Objects.requireNonNull(ctx.getConfiguration());
    }
    // end::constructor[]

    // tag::connect[]
    @Override
    public void connect() {
        // No connection setup needed.
    }
    // end::connect[]

    // tag::execute[]
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
    // end::execute[]
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
    // tag::executeCountCharacters[]
    private void executeCountCharacters(Message message, WritableMessage response, boolean countWhitespaces)
            throws XMLStreamException, IOException {

        String text = extractTextFromXml(message.getXmlBody());
        long characterCount = countCharacters(text, countWhitespaces);
        try (var os = response.getBodyOutputStream()) {
            String outXml = "<ROOT><COUNT>%d</COUNT></ROOT>".formatted(characterCount);
            os.write(outXml.getBytes(StandardCharsets.UTF_8));
        }
    }
    // end::executeCountCharacters[]
    /**
     * Extracts text content from the ROOT element in an XML document.
     *
     * @param reader the XML event reader
     * @return the extracted text
     * @throws XMLStreamException if an XML parsing error occurs
     */
    // tag::extractTextFromXml[]
    private String extractTextFromXml(XMLEventReader reader) throws XMLStreamException {

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()
                    && "ROOT".equals(event.asStartElement().getName().getLocalPart())) {
                if (reader.hasNext()) {
                    // Skip whitespace characters
                    do {
                        event = reader.nextEvent();
                    } while (event.isCharacters() && event.asCharacters().isWhiteSpace());
                    if (event.isStartElement()
                            && "CONTENT".equals(event.asStartElement().getName().getLocalPart())) {
                        event = reader.nextEvent();
                        if (event.isCharacters()) {
                            return event.asCharacters().getData();
                        }
                    }
                }
                return "";
            }
        }

        return "";
    }
    // end::extractTextFromXml[]
    /**
     * Counts the characters in a text based on the counting rules.
     *
     * @param text the text to analyze
     * @param countWhitespaces whether to include whitespace in the count
     * @return the number of characters according to the counting rules
     */
    // tag::countCharacters[]
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
    // end::countCharacters[]

    // tag::isConnected[]
    @Override
    public boolean isConnected() {
        return true;
    }
    // end::isConnected[]

    // tag::close[]
    @Override
    public void close() {
        // No resources to release.
    }
    // end::close[]
}
