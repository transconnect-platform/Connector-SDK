/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.producer.jaxb;

import com.example.connector.util.XmlUtils;
import io.transconnect.connector.api.Configuration;
import io.transconnect.connector.api.TransconnectConnectorException;
import io.transconnect.connector.api.message.MessageFactory;
import io.transconnect.connector.api.message.WritableMessage;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnectorListener;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLOutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeConnection implements ProducerConnection {

    private static final Logger LOG = LoggerFactory.getLogger(DateTimeConnection.class);
    private final XMLOutputFactory outputFactory;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;
    private final Long interval;
    private final Clock clock;
    private final String messageType;
    private ProducerConnectorListener listener;

    protected DateTimeConnection(Configuration configuration, Clock clock) {
        this.clock = clock;
        outputFactory = XMLOutputFactory.newFactory();
        interval = configuration.getLong("interval").map(Math::abs).orElse(5L);
        messageType = configuration.getString("$messageType").orElse("Default");
    }

    /**
     * Creates a scheduled executor to send messages in a specified interval.
     *
     * @param factory The message factory used to generate new messages
     * @param connectorListener Acts as the message receiver of the generated messages.
     */
    @Override
    public void connect(MessageFactory<WritableMessage> factory, ProducerConnectorListener connectorListener) {
        this.listener = connectorListener;

        if (interval != null) {
            task = executor.scheduleAtFixedRate(
                    () -> createMessage(factory, connectorListener), 0, interval, TimeUnit.SECONDS);
            LOG.info("creating periodic messages every {}s", interval);
            connectorListener.onConnect();
        } else {
            connectorListener.onError(new IllegalStateException("Invalid configuration"));
        }
    }

    /**
     * Creates an XML message with current timestamp.
     */
    private void createMessage(MessageFactory<WritableMessage> factory, ProducerConnectorListener connectorListener) {
        LOG.info("Creating new message");
        WritableMessage message = factory.createMessage();
        var result = new com.example.connector.datetime.execute.out.ROOT();
        result.setDATETIME(LocalDateTime.now(clock).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        try {
            XmlUtils.marshalMessage(message, result);
        } catch (TransconnectConnectorException e) {
            LOG.error("Error creating message", e);
            connectorListener.onError(e);
        }
    }

    /**
     * Returns true as long as messages will be produced, otherwise false.
     *
     * @return true as long as messages will be produced, otherwise false
     */
    @Override
    public boolean isConnected() {
        return !executor.isShutdown();
    }

    /**
     * Stops the production of new messages.
     */
    @Override
    public void close() {
        LOG.info("Closing DateTimeConnection");

        if (task != null) {
            task.cancel(false);
        }

        executor.shutdown();

        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.info("Connection closed successfully");
            } else {
                LOG.warn("Connection closed, but a scheduled thread was still running");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while closing executor", e);
            Thread.currentThread().interrupt();
        } finally {
            if (listener != null) {
                listener.onDisconnect();
            }
        }
    }
}
