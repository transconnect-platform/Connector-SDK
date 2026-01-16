package com.example.connector.producer;

import io.transconnect.connector.api.Context;
import io.transconnect.connector.api.message.MessageFactory;
import io.transconnect.connector.api.message.WritableMessage;
import io.transconnect.connector.api.producer.ProducerConnection;
import io.transconnect.connector.api.producer.ProducerConnectorListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DateTimeConnection implements ProducerConnection {

    // tag::executor-field[]
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> task;
    private ProducerConnectorListener listener;
    // end::executor-field[]
    // tag::constructor[]
    private final long interval;
    private final Clock clock;

    public DateTimeConnection(Context ctx, Clock clock) {
        this.clock = clock;
        interval = ctx.getConfiguration().getLong("interval").map(Math::abs).orElse(5L);
    }
    // end::constructor[]

    /**
     * Creates a scheduled executor to send messages in a specified interval.
     *
     * @param factory The message factory used to generate new messages
     * @param connectorListener Acts as the message receiver of the generated messages.
     */
    // tag::connect-method[]
    @Override
    public void connect(MessageFactory<WritableMessage> factory, ProducerConnectorListener connectorListener) {
        this.listener = connectorListener;
        task = executor.scheduleAtFixedRate(() -> createMessage(factory), 0, interval, TimeUnit.SECONDS);
        connectorListener.onConnect();
    }
    // end::connect-method[]

    /**
     * Creates an XML message with current timestamp.
     */
    // tag::createMessage-method[]
    private void createMessage(MessageFactory<WritableMessage> factory) {
        WritableMessage message = factory.createMessage();

        try (var out = message.getBodyOutputStream()) {
            String outXml = "<ROOT><DATETIME>%s</DATETIME></ROOT>".formatted(LocalDateTime.now(clock));
            out.write(outXml.getBytes(StandardCharsets.UTF_8));
            listener.onMessage(message);
        } catch (IOException e) {
            listener.onError(e);
        }
    }
    // end::createMessage-method[]

    /**
     * Returns true as long as messages will be produced, otherwise false.
     *
     * @return true as long as messages will be produced, otherwise false
     */
    // tag::isConnected-method[]
    @Override
    public boolean isConnected() {
        return !executor.isShutdown();
    }
    // end::isConnected-method[]

    /**
     * Stops the production of new messages.
     */
    // tag::close-method[]
    @Override
    public void close() {
        if (task != null) {
            task.cancel(false);
        }
        executor.shutdown();
        if (listener != null) {
            listener.onDisconnect();
        }
    }
    // end::close-method[]
}
