/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * A message describes a unit of work for a Connector. A message contains all necessary information for the Connector to
 * execute an interaction.
 */
public interface Message {

    /**
     * Returns the ID of this message.
     *
     * @return the ID of this message
     */
    Long getId();

    /**
     * Returns the type of the message.
     *
     * @return the type of the message
     */
    String getMessageType();

    /**
     * Returns the Body of the Message as XMLEventReader. The reader must be closed by the calling method. This method
     * can only be called once.
     *
     * @return the body of the Message
     * @throws XMLStreamException if there is a problem to read the body with an XML Reader.
     */
    XMLEventReader getXmlBody() throws XMLStreamException;

    /**
     * Returns a collection of IDs from message attachments.
     *
     * @return a collection of IDs from message attachments. The collection can be empty but never null
     */
    Collection<String> getAttachmentIds();

    /**
     * Returns a stream of the attachment body with the given id.
     *
     * @param id the attachment ID
     * @return the attachment body as {@link InputStream}
     * @throws IOException if there was a problem with creating the InputStream
     * @throws IllegalArgumentException if there is no attachment with the given ID
     */
    InputStream getAttachment(String id) throws IOException, IllegalArgumentException;

    /**
     * Returns the size of the attachment with the given id.
     *
     * @param id the attachment ID
     * @return the size of the attachment in bytes
     * @throws IOException if there was a problem getting the size
     * @throws IllegalArgumentException if there is no attachment with the given ID
     */
    long getAttachmentSize(String id) throws IOException;
}
