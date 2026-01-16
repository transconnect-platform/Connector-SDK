/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.MockContext;
import io.transconnect.connector.MockWritableMessage;
import io.transconnect.connector.api.message.Message;
import io.transconnect.connector.api.message.MessageFactory;
import io.transconnect.connector.api.message.WritableMessage;
import io.transconnect.connector.api.producer.ProducerConnectorListener;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DateTimeConnectionTest {

    @Test
    void connectShouldProduceMessagesWithinInterval() throws Exception {
        // create connection context
        final String MESSAGE_TYPE_VALUE = "DateTimeMessage";
        MockContext context =
                new MockContext(new MockConfig(Map.of("interval", 1L, "$messageType", MESSAGE_TYPE_VALUE)));
        Clock fixedClock = Mockito.mock(Clock.class);
        Mockito.when(fixedClock.instant()).thenReturn(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2));
        Mockito.when(fixedClock.getZone()).thenReturn(ZoneId.of("UTC"));

        // create connection
        try (DateTimeConnection cut = new DateTimeConnection(context, fixedClock)) {
            // create a message factory that produces messages that can be written to
            MessageFactory<WritableMessage> factory = Mockito.mock(MessageFactory.class);
            Mockito.when(factory.createMessage()).thenAnswer(call -> new MockWritableMessage());

            // create listener to collect messages
            CompletableFuture<List<Message>> messageFuture = new CompletableFuture<>();
            ProducerConnectorListener listener = new ProducerConnectorListener() {

                private final List<Message> recordedMessages = new ArrayList<>();

                @Override
                public Future<Message> onMessage(Message message) {
                    recordedMessages.add(message);

                    if (recordedMessages.size() == 2) {
                        messageFuture.complete(recordedMessages);
                    }
                    return null;
                }

                @Override
                public void onMessageWithoutResult(Message message) {}

                @Override
                public void onError(Throwable throwable) {
                    messageFuture.completeExceptionally(throwable);
                }

                @Override
                public void onConnect() {}

                @Override
                public void onDisconnect() {}
            };

            // connect
            cut.connect(factory, listener);

            // wait for messages to be produced
            var messages = messageFuture.get(10, TimeUnit.SECONDS);

            // evaluate results
            var firstMessage = (MockWritableMessage) messages.get(0);
            assertEquals(
                    "<ROOT><DATETIME>1970-01-01T00:00:01</DATETIME></ROOT>",
                    new String(firstMessage.getBody(), StandardCharsets.UTF_8));
            var secondMessage = (MockWritableMessage) messages.get(1);
            assertEquals(
                    "<ROOT><DATETIME>1970-01-01T00:00:02</DATETIME></ROOT>",
                    new String(secondMessage.getBody(), StandardCharsets.UTF_8));
        }
    }
}