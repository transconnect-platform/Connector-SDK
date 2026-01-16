/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector;

import io.transconnect.connector.api.message.Message;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * A mock implementation of the {@link Message} interface.
 */
public final class MockMessage implements Message {
    /**
     * The body of the message.
     */
    private final String body;

    /**
     * The attachments of the message.
     */
    private final Map<String, ByteBuffer> attachments = new HashMap<>();

    /**
     * Creates a new instance of the mock message with the given body.
     * @param body The body of the message.
     */
    public MockMessage(String body) {
        this.body = body;
    }

    /**
     * Adds an attachment to the message.
     * @param id The ID of the attachment.
     * @param attachment The attachment.
     */
    public void addAttachment(String id, byte[] attachment) {
        attachments.put(id, ByteBuffer.wrap(attachment));
    }

    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public String getMessageType() {
        return "";
    }

    @Override
    public XMLEventReader getXmlBody() throws XMLStreamException {
        return MessageUtils.getXmlEventReader(body);
    }

    @Override
    public Collection<String> getAttachmentIds() {
        return attachments.keySet();
    }

    @Override
    public InputStream getAttachment(String id) throws IOException, IllegalArgumentException {
        ByteBuffer attachment = attachments.get(id);
        if (attachment == null) {
            throw new IllegalArgumentException("Unable to find attachment with id " + id);
        }
        return new ByteArrayInputStream(attachment.array());
    }

    @Override
    public long getAttachmentSize(String id) throws IOException, IllegalArgumentException {
        ByteBuffer attachment = attachments.get(id);
        if (attachment == null) {
            throw new IllegalArgumentException("Unable to find attachment with id " + id);
        }
        return attachment.capacity();
    }
}
