/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to work with classpath resources (files and directories).
 */
public final class ClasspathUtils {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathUtils.class);

    /**
     * private Constructor to prevent creating an instance of util class.
     */
    private ClasspathUtils() {
        // utility class, no instances
    }

    /**
     * Loads a resource from the classpath as an InputStream.
     *
     * @param resourcePath Path within the classpath, e.g., "io/transconnect/sample/test.xml"
     * @return InputStream if found; otherwise throws IllegalArgumentException
     */
    public static InputStream loadResource(String resourcePath) {

        Objects.requireNonNull(resourcePath, "The resourcePath must not be null");
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    }

    /**
     * Checks whether a resource exists on the classpath.
     *
     * @param resourcePath Path to the resource within the classpath
     * @return true if the resource exists, false otherwise
     */
    public static boolean resourceExists(String resourcePath) {

        Objects.requireNonNull(resourcePath, "The resourcePath must not be null");
        return Thread.currentThread().getContextClassLoader().getResource(resourcePath) != null;
    }

    /**
     * Checks whether a classpath resource points to a directory.
     * Works only if the resource is available as a real folder (not inside a JAR).
     *
     * @param resourcePath Path to the resource within the classpath
     * @return true if the resource exists and is a directory, false otherwise
     */
    public static boolean isDirectory(String resourcePath) {

        Objects.requireNonNull(resourcePath, "The resourcePath must not be null");

        // a trailing slash is not needed to resolve the directory resource
        String normalized =
                resourcePath.endsWith("/") ? resourcePath.substring(0, resourcePath.length() - 1) : resourcePath;

        URL url = Thread.currentThread().getContextClassLoader().getResource(normalized);
        if (url == null || !"file".equals(url.getProtocol())) {
            return false;
        }

        try {
            return Files.isDirectory(Paths.get(url.toURI()));
        } catch (URISyntaxException exception) {
            LOGGER.debug("Could not convert resource URL to a path: {}", exception.getMessage());
            return false;
        }
    }

    /**
     * Lists all regular files under a classpath directory.
     * Works only if the directory is available as a real folder (not inside a JAR).
     *
     * @param folderPath Classpath-relative folder path
     * @return Stream of Paths for regular files inside the directory
     */
    public static Stream<Path> listFiles(String folderPath) {
        return listFiles(folderPath, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Lists all regular files under a classpath directory.
     * Works only if the directory is available as a real folder (not inside a JAR).
     *
     * @param folderPath Classpath-relative folder path
     * @param classLoader specific Classloader
     * @return Stream of Paths for regular files inside the directory
     */
    public static Stream<Path> listFiles(String folderPath, ClassLoader classLoader) {
        Objects.requireNonNull(folderPath, "The folderPath must not be null");
        Objects.requireNonNull(classLoader, "The classLoader must not be null");

        try {
            Enumeration<URL> urls = classLoader.getResources(folderPath);
            List<Path> result = new ArrayList<>();

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();

                if ("file".equals(url.getProtocol())) {
                    Path dir = Paths.get(url.toURI());
                    if (Files.isDirectory(dir)) {
                        try (Stream<Path> stream = Files.list(dir)) {
                            stream.filter(Files::isRegularFile).forEach(result::add);
                        } catch (Exception exception) {
                            LOGGER.debug("Could not add URL to Stream: {}", exception.getMessage());
                        }
                    }
                }
            }

            return result.stream();
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to list resources from classpath folder: " + folderPath, e);
        }
    }

    /**
     * Converts the classpath file to the real file path.
     *
     * @param resourceName File name in the classpath
     * @return Path to the converted regular file
     */
    public static Path convertClasspathToRegularPath(String resourceName) {
        return convertClasspathToRegularPath(
                resourceName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Converts the classpath file to the real file path.
     *
     * @param resourceName File name in the classpath
     * @param classLoader specific class loader
     * @return Path to the converted regular file
     */
    public static Path convertClasspathToRegularPath(String resourceName, ClassLoader classLoader) {

        Objects.requireNonNull(resourceName, "No resource name is given");
        Objects.requireNonNull(classLoader, "No class loader is given");

        URL resource = classLoader.getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }

        if (!resource.getProtocol().equals("file")) {
            throw new IllegalArgumentException("Resource is not a file: " + resourceName);
        }

        try {
            return Paths.get(resource.toURI());
        } catch (Exception exception) {
            throw new RuntimeException("URI Syntax wrong: " + exception.getMessage(), exception);
        }
    }
}
