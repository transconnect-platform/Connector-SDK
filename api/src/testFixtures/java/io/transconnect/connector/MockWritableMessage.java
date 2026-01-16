/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector;

import io.transconnect.connector.api.message.WritableMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * A mock implementation of the {@link WritableMessage} interface.
 */
public final class MockWritableMessage implements WritableMessage {

    /**
     * The body of the message.
     */
    private final ByteArrayOutputStream bodyOutStream = new ByteArrayOutputStream();

    /**
     * The attachments of the message.
     */
    private final Map<String, ByteArrayOutputStream> attachments = new HashMap<>();

    @Override
    public OutputStream getBodyOutputStream() {
        return bodyOutStream;
    }

    @Override
    public OutputStream getAttachmentOutputStream(String attachmentId) {
        return attachments.computeIfAbsent(attachmentId, k -> new ByteArrayOutputStream());
    }

    @Override
    public Long getId() {
        // the mock implementation always returns the message id 0
        return 0L;
    }

    @Override
    public String getMessageType() {
        // the mock implementation returns an empty message type
        return "";
    }

    @Override
    public XMLEventReader getXmlBody() throws XMLStreamException {
        return XMLInputFactory.newInstance()
                .createXMLEventReader(new ByteArrayInputStream(bodyOutStream.toByteArray()));
    }

    @Override
    public Collection<String> getAttachmentIds() {
        return attachments.keySet();
    }

    @Override
    public InputStream getAttachment(String id) throws IOException, IllegalArgumentException {

        if (attachments.containsKey(id)) {
            return new ByteArrayInputStream(attachments.get(id).toByteArray());
        }
        throw new IllegalArgumentException("No attachment found with ID " + id);
    }

    @Override
    public long getAttachmentSize(String id) throws IOException, IllegalArgumentException {
        if (attachments.containsKey(id)) {
            return attachments.get(id).size();
        }
        throw new IllegalArgumentException("No attachment found with ID " + id);
    }

    /**
     * Returns the body of the message as byte array.
     * @return a byte array containing the body of the message
     */
    public byte[] getBody() {
        return bodyOutStream.toByteArray();
    }
}
