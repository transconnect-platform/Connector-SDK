/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

public class MessageUtils {
    /**
     * Get an {@link XMLEventReader} for the given XML body.
     * @param body The XML body
     * @return An {@link XMLEventReader} for the given XML body
     * @throws XMLStreamException In case anything goes wrong
     */
    public static XMLEventReader getXmlEventReader(String body) throws XMLStreamException {
        return XMLInputFactory.newInstance()
                .createXMLEventReader(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Suppress instantiation.
     */
    private MessageUtils() {}
}
