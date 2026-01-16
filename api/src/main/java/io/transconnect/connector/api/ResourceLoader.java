/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * This class loads resources from the classpath.
 */
public final class ResourceLoader {

    /**
     * Private utils constructor.
     */
    private ResourceLoader() {}

    /**
     * Reads the Resource located by the URI with the given class loader.
     *
     * @param classLoader the class loader to use for loading the resource
     * @param resourceUri the URI to read
     * @return the content of the resource
     * @throws IOException if the resource could not be read.
     */
    public static byte[] readResource(ClassLoader classLoader, URI resourceUri) throws IOException {

        try (InputStream resourceStream = classLoader.getResourceAsStream(resourceUri.toString())) {

            if (null == resourceStream) {
                throw new IOException("Can not read file from URI '" + resourceUri + "'.");
            }
            return resourceStream.readAllBytes();
        }
    }
}
