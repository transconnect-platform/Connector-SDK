/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package com.example.connector.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.transconnect.connector.MockConfig;
import io.transconnect.connector.MockContext;
import io.transconnect.connector.MockMessage;
import io.transconnect.connector.MockWritableMessage;
import io.transconnect.connector.api.TransconnectConnectorException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CountCharactersConnectionTest {

    @Test
    public void executeCountCharactersWithoutSpacesShouldProduceCorrectResult() throws Exception {
        MockContext ctx = new MockContext();
        executeCountCharacters(ctx, "Some text with whitespaces", 23L);
    }

    @Test
    public void executeCountCharactersWithSpacesShouldProduceCorrectResult() throws Exception {
        MockContext ctx = new MockContext(new MockConfig((Map.of("countWhitespaces", true))));
        executeCountCharacters(ctx, "Some other text with whitespaces", 32L);
    }

    private void executeCountCharacters(MockContext context, String text, long expectedResult)
            throws URISyntaxException, TransconnectConnectorException {
        // create connection
        try (CountCharactersConnection cut = new CountCharactersConnection(context)) {

            // connect
            cut.connect();

            // create input message that the connector will consume
            MockMessage message = new MockMessage(String.format(
                    """
                <ROOT>
                    <CONTENT>%s</CONTENT>
                </ROOT>
                """,
                    text));

            // create a writable message to receive the response
            MockWritableMessage response = new MockWritableMessage();

            // execute the interaction
            cut.execute(new URI("countCharacters"), message, response);

            // evaluate results
            assertEquals(
                    String.format("<ROOT><COUNT>%d</COUNT></ROOT>", expectedResult), new String(response.getBody()));
        }
    }
}
