/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.message;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * Adapts an {@link XMLEventReader}, which does not implement {@link AutoCloseable}, so it can be
 * used in a try-with-resources statement.
 *
 * <p>{@link Message#getXmlBody()} returns an {@code XMLEventReader} that must be closed by the
 * caller. Wrapping it in this record avoids having to close the reader manually in every
 * success and error path.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * try (var reader = new CloseableXMLEventReader(message.getXmlBody())) {
 *     T result = unmarshaller.unmarshal(reader.delegate());
 * } catch (JAXBException | XMLStreamException e) {
 *     throw new TransconnectConnectorException("Failed to unmarshal message", e);
 * }
 * }</pre>
 *
 * @param delegate the {@code XMLEventReader} to close when this record is closed
 */
public record CloseableXMLEventReader(XMLEventReader delegate) implements AutoCloseable {

    /**
     * Closes the wrapped {@link XMLEventReader}.
     *
     * @throws XMLStreamException if the reader could not be closed
     */
    @Override
    public void close() throws XMLStreamException {
        delegate.close();
    }
}
