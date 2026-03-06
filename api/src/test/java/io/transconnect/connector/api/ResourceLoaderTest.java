/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public final class ResourceLoaderTest {

    private ClassLoader classLoader;

    @BeforeEach
    void before() {
        classLoader = getClass().getClassLoader();
    }

    static Stream<Arguments> validResources() {
        return Stream.of(Arguments.of(URI.create("testfile.txt"), "Expected content of testfile.txt"));
    }

    @ParameterizedTest
    @MethodSource("validResources")
    void testReadResource_Success(URI resourceUri, String expectedContent) throws IOException {
        byte[] content = ResourceLoader.readResource(classLoader, resourceUri);
        String actualContent = new String(content, StandardCharsets.UTF_8);
        assertEquals(expectedContent, actualContent.trim());
    }

    @Test
    void testReadResource_FileNotFound() {
        URI invalidUri = URI.create("nonexistent.txt");
        assertThrows(IOException.class, () -> ResourceLoader.readResource(classLoader, invalidUri));
    }
}
