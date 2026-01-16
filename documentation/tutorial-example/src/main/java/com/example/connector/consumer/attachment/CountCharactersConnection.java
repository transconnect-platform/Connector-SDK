package com.example.connector.consumer.attachment;

import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.consumer.ConsumerConnection;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.WritableMessage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A consumer connection implementation that demonstrates working with attachments.
 * This class extends the character counting functionality to process text from
 * both XML body and attachments.
 */
public final class CountCharactersConnection implements ConsumerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(CountCharactersConnection.class);
    private static final String INTERACTION_COUNT_CHARS_URI = "countCharacters";
    private static final String INTERACTION_PROCESS_FILE_URI = "processFile";

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
     * Executes the interaction based on the interaction ID.
     */
    @Override
    public void execute(URI interactionId, Message message, WritableMessage response)
            throws TransconnectConnectorException {

        try {
            if (INTERACTION_COUNT_CHARS_URI.equalsIgnoreCase(interactionId.toString())) {
                var countWhitespaces =
                        configuration.getBoolean("countWhitespaces").orElse(Boolean.FALSE);
                executeCountCharactersWithAttachments(message, response, countWhitespaces);
            } else if (INTERACTION_PROCESS_FILE_URI.equalsIgnoreCase(interactionId.toString())) {
                processFileAttachment(message, response);
            } else {
                throw new TransconnectConnectorException("Unknown Interaction: '%s'".formatted(interactionId));
            }
        } catch (XMLStreamException | IOException e) {
            throw new TransconnectConnectorException("Error processing message", e);
        }
    }

    // tag::executeCountCharactersWithAttachments[]
    /**
     * Counts characters in both XML body and text attachments.
     */
    private void executeCountCharactersWithAttachments(
            Message message, WritableMessage response, boolean countWhitespaces)
            throws XMLStreamException, IOException {

        // Get text from XML body
        String bodyText = extractTextFromXml(message.getXmlBody());

        // Get text from attachment if it exists
        StringBuilder textBuilder = new StringBuilder(bodyText);

        // Check for text attachments and add their content
        if (message.getAttachmentIds().contains("additionalText")) {
            String attachmentText = readTextAttachment(message, "additionalText");
            textBuilder.append(attachmentText);
        }

        String combinedText = textBuilder.toString();
        LOG.debug("Processing combined text: {}", combinedText);

        // Count characters
        long characterCount = countCharacters(combinedText, countWhitespaces);

        // Write response to body
        try (var os = response.getBodyOutputStream()) {
            String outXml = "<ROOT><COUNT>%d</COUNT></ROOT>".formatted(characterCount);
            os.write(outXml.getBytes(StandardCharsets.UTF_8));
        }

        // Also write a detailed report as an attachment
        writeDetailedReport(
                response,
                bodyText,
                message.getAttachmentIds().contains("additionalText")
                        ? readTextAttachment(message, "additionalText")
                        : "",
                characterCount,
                countWhitespaces);
    }
    // end::executeCountCharactersWithAttachments[]

    // tag::readTextAttachment[]
    /**
     * Reads the content of a text attachment as a String.
     */
    private String readTextAttachment(Message message, String attachmentId) throws IOException {
        if (message.getAttachmentIds().contains(attachmentId)) {
            return new String(message.getAttachment(attachmentId).readAllBytes(), StandardCharsets.UTF_8);
        }
        return "";
    }
    // end::readTextAttachment[]

    // tag::readTextFileLines[]
    /**
     * Reads text attachment content and returns it as a list of lines.
     */
    private List<String> readLinesFromAttachment(Message message, String attachmentId) throws IOException {
        List<String> lines = new ArrayList<>();

        if (message.getAttachmentIds().contains(attachmentId)) {
            String content = readTextAttachment(message, attachmentId);

            // Split content by newlines
            String[] lineArray = content.split("\n");
            lines.addAll(Arrays.asList(lineArray));

            LOG.debug("Read {} lines from attachment '{}'", lines.size(), attachmentId);
        }

        return lines;
    }
    // end::readTextFileLines[]

    // tag::readBinaryAttachment[]
    /**
     * Reads a binary attachment and returns the content as a byte array.
     */
    private byte[] readBinaryAttachment(Message message, String attachmentId) throws IOException {
        if (message.getAttachmentIds().contains(attachmentId)) {
            // Read all bytes from the attachment
            return message.getAttachment(attachmentId).readAllBytes();
        }
        return new byte[0];
    }
    // end::readBinaryAttachment[]

    // tag::processLargeAttachment[]
    /**
     * Processes a large attachment in chunks to avoid memory issues.
     */
    private void processLargeAttachment(Message message, String attachmentId) throws IOException {
        if (message.getAttachmentIds().contains(attachmentId)) {
            try (InputStream is = message.getAttachment(attachmentId)) {
                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = is.read(buffer)) != -1) {
                    // Process buffer bytes
                    totalBytes += bytesRead;
                    // Example: Just count bytes
                }

                LOG.debug("Processed {} bytes from attachment '{}'", totalBytes, attachmentId);
            }
        }
    }
    // end::processLargeAttachment[]

    // tag::writeTextAttachment[]
    /**
     * Writes text content to an attachment.
     */
    private void writeTextAttachment(WritableMessage response, String attachmentId, String content) throws IOException {
        try (var outStream = response.getAttachmentOutputStream(attachmentId)) {
            outStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
    // end::writeTextAttachment[]

    // tag::writeBinaryAttachment[]
    /**
     * Writes binary data to an attachment.
     */
    private void writeBinaryAttachment(WritableMessage response, String attachmentId, byte[] data) throws IOException {
        try (var outStream = response.getAttachmentOutputStream(attachmentId)) {
            outStream.write(data);
        }
    }
    // end::writeBinaryAttachment[]

    /**
     * Writes a detailed character count report as an attachment.
     */
    private void writeDetailedReport(
            WritableMessage response, String bodyText, String attachmentText, long totalCount, boolean countWhitespaces)
            throws IOException {

        long bodyCount = countCharacters(bodyText, countWhitespaces);
        long attachmentCount = countCharacters(attachmentText, countWhitespaces);

        String report = String.format(
                "Character Count Report\n" + "=====================\n"
                        + "XML Body: %d characters\n"
                        + "Attachment: %d characters\n"
                        + "Total: %d characters\n"
                        + "Count whitespaces: %s",
                bodyCount, attachmentCount, totalCount, countWhitespaces ? "Yes" : "No");

        writeTextAttachment(response, "report", report);
    }

    /**
     * Process a file attachment based on its type.
     */
    private void processFileAttachment(Message message, WritableMessage response) throws IOException {

        if (!message.getAttachmentIds().contains("file")) {
            try (var os = response.getBodyOutputStream()) {
                os.write("<ROOT><ERROR>No file attachment found</ERROR></ROOT>".getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        // Get file attachment
        byte[] fileData = readBinaryAttachment(message, "file");

        // Process based on file type (simplified example)
        String fileInfo =
                String.format("File Information\n" + "===============\n" + "Size: %d bytes\n", fileData.length);

        // Write information back as an attachment
        writeTextAttachment(response, "fileInfo", fileInfo);

        // Write response body
        try (var os = response.getBodyOutputStream()) {
            os.write(("<ROOT><PROCESSED>File processed successfully</PROCESSED></ROOT>")
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Extracts text content from the ROOT element in an XML document.
     */
    private String extractTextFromXml(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()
                    && "ROOT".equals(event.asStartElement().getName().getLocalPart())) {
                if (reader.hasNext()) {
                    event = reader.nextEvent();
                    if (event.isCharacters()) {
                        return event.asCharacters().getData();
                    }
                }
                return "";
            }
        }
        return "";
    }

    /**
     * Counts the characters in a text based on the counting rules.
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
