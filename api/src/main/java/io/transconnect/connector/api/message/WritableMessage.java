/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Message where body and attachments can be written. An implementation can be used to respond to a message or create a
 * new message in a producing connector.
 */
public interface WritableMessage extends Message {

    /**
     * Returns the {@link OutputStream} that can be used to write the message body. The stream must be closed by the
     * caller.
     *
     * @return an open OutputStream
     * @throws IOException if there was a problem during the creation of the stream.
     */
    OutputStream getBodyOutputStream() throws IOException;

    /**
     * Returns the {@link OutputStream} that can be used to write the attachment with the given attachmentId. The stream
     * must be closed by the caller.
     *
     * @param attachmentId the ID of the attachment to write
     * @return an open OutputStream
     * @throws IOException if there was a problem during the creation of the stream.
     */
    OutputStream getAttachmentOutputStream(String attachmentId) throws IOException;
}
