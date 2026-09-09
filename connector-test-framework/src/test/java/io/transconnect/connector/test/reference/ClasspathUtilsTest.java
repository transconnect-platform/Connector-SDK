/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */

package io.transconnect.connector.test.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ClasspathUtilsTest {

    /**
     * Tests the loading of a resource from classpath.
     * @throws IOException if the test fails
     */
    @Test
    public void testLoadResource() throws IOException {
        // Test with existing resource
        try (InputStream is =
                ClasspathUtils.loadResource("io/transconnect/connector/test/reference/ClasspathUtils/test.xml")) {
            assertNotNull(is, "Resource should be found");

            assertNull(ClasspathUtils.loadResource(
                    "io/transconnect/connector/test/reference/ClasspathUtils/non-existing.txt"));
        }
    }

    /**
     * Tests the exist method with existing resource.
     */
    @Test
    public void testResourceExists() {
        assertTrue(
                ClasspathUtils.resourceExists("io/transconnect/connector/test/reference/ClasspathUtils/test.xml"),
                "Resource should exist");
        assertFalse(
                ClasspathUtils.resourceExists(
                        "io/transconnect/connector/test/reference/ClasspathUtils/non-existing.txt"),
                "Resource should not exist");
    }

    /**
     * Tests the listing of files on classpath.
     */
    @Test
    public void testListFiles() {
        Stream<Path> files = ClasspathUtils.listFiles("io/transconnect/connector/test/reference/ClasspathUtils");
        assertNotNull(files, "Stream of files should not be null");

        long count = files.count();
        assertEquals(2, count, "There should be at least one file in the directory");
    }

    /**
     * Tests the throwing IllegalStateExceptiom
     */
    @Test
    public void testListFilesNoUrls() throws IOException {
        ClassLoader classLoaderMock = mock(ClassLoader.class);
        // behavior of getResource
        when(classLoaderMock.getResources(any())).thenThrow(new IOException("IO Exception thrown"));

        assertThrows(
                IllegalStateException.class,
                () -> ClasspathUtils.listFiles(
                        "io/transconnect/connector/test/reference/ClasspathUtils", classLoaderMock));
    }

    /**
     * Tests with URL and no file protocol
     */
    @Test
    public void testListFilesNoUrlFileProtocol() throws IOException {
        ClassLoader classLoaderMock = mock(ClassLoader.class);
        // behavior of getResource
        URL jarUrl = new URL("jar:file:/some/file.jar!/");
        Enumeration<URL> urls = Collections.enumeration(List.of(jarUrl));
        when(classLoaderMock.getResources(any())).thenReturn(urls);

        // run the method
        Stream<Path> result =
                ClasspathUtils.listFiles("io/transconnect/connector/test/reference/ClasspathUtils", classLoaderMock);

        assertEquals(0, result.count());
    }

    /**
     * Tests with no directory is given
     */
    @Test
    public void testListFilesNoDirectory() throws IOException {
        // simulate the url
        URL fileUrl = new URL("file:/some/fake/path");
        Enumeration<URL> urls = Collections.enumeration(Collections.singletonList(fileUrl));

        // mock the class loader
        ClassLoader classLoaderMock = mock(ClassLoader.class);
        when(classLoaderMock.getResources(any())).thenReturn(urls);

        // mock static Files.isDirectory
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            // mock behavior
            mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(false);

            // run the method
            Stream<Path> result = ClasspathUtils.listFiles("/some/fake/path", classLoaderMock);

            // check the result
            assertNotNull(result);
            assertEquals(0, result.count());
        }
    }

    @Test
    void testListFilesNoListingDirectory() throws Exception {
        // simulate the url
        URL fileUrl = new URL("file:/some/fake/path");
        Enumeration<URL> urls = Collections.enumeration(Collections.singletonList(fileUrl));

        // mock the class loader
        ClassLoader mockClassLoader = mock(ClassLoader.class);
        when(mockClassLoader.getResources(any())).thenReturn(urls);

        // mock static methods
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            // go over isDirectory
            mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);

            // throw an exception at Files.list
            mockedFiles.when(() -> Files.list(any())).thenThrow(new NotDirectoryException("Not a directory"));

            try (Stream<Path> path = ClasspathUtils.listFiles("no-folder.txt", mockClassLoader)) {
                assertNotNull(path);
                assertEquals(0, path.count());
            }
        }
    }

    /**
     * Tests the converting of a classpath path to regular path on disk.
     */
    @Test
    public void testConvertClasspathToRegularPath() {
        String resource = "io/transconnect/connector/test/reference/ClasspathUtils/test.xml";
        Path resourcePath = ClasspathUtils.convertClasspathToRegularPath(resource);

        String relative = "build/resources/test/io/transconnect/connector/test/reference/ClasspathUtils/test.xml";
        Path absolutePath = Paths.get(relative).toAbsolutePath();
        assertEquals(absolutePath, resourcePath);
    }

    /**
     * Tests the converting of a classpath path to regular path on disk and the path is invalid
     */
    @Test
    public void testConvertClasspathToRegularPathWithNoResource() {

        ClassLoader classLoaderMock = mock(ClassLoader.class);
        // mock behavior
        when(classLoaderMock.getResource(any())).thenReturn(null);

        String resource = "io/transconnect/connector/test/reference/ClasspathUtils/test.xml";
        assertThrows(
                IllegalArgumentException.class,
                () -> ClasspathUtils.convertClasspathToRegularPath(resource, classLoaderMock));
    }

    /**
     * Tests the converting of a classpath path to regular path on disk and the path is not a file
     */
    @Test
    public void testConvertClasspathToRegularPathNoFileResource() {

        ClassLoader classLoaderMock = mock(ClassLoader.class);
        URL resourceMock = mock(URL.class);

        // mock behavior
        when(classLoaderMock.getResource(any())).thenReturn(resourceMock);
        when(resourceMock.getProtocol()).thenReturn("dir");

        String resource = "io/transconnect/connector/test/reference/ClasspathUtils/test.xml";
        assertThrows(
                IllegalArgumentException.class,
                () -> ClasspathUtils.convertClasspathToRegularPath(resource, classLoaderMock));
    }

    /**
     * Tests the converting of a classpath path to regular path on disk and the url is invalid
     */
    @Test
    public void testConvertClasspathToRegularPathInvalidUrl() {

        ClassLoader classLoaderMock = mock(ClassLoader.class);
        URL resourceMock = mock(URL.class);

        // mock behavior
        when(classLoaderMock.getResource(any())).thenReturn(resourceMock);
        when(resourceMock.getProtocol()).thenReturn("file");

        String resource = "path/to/invalid resource.txt";
        assertThrows(
                RuntimeException.class, () -> ClasspathUtils.convertClasspathToRegularPath(resource, classLoaderMock));
    }

    /**
     * Tests that an existing folder on the classpath is recognized as a directory,
     * with and without a trailing slash.
     */
    @Test
    public void testIsDirectory() {
        assertTrue(
                ClasspathUtils.isDirectory("io/transconnect/connector/test/reference/ClasspathUtils"),
                "An existing folder should be recognized as a directory");
        assertTrue(
                ClasspathUtils.isDirectory("io/transconnect/connector/test/reference/ClasspathUtils/"),
                "A trailing slash should be handled");
    }

    /**
     * Tests that a regular file is not recognized as a directory.
     */
    @Test
    public void testIsDirectoryForFile() {
        assertFalse(
                ClasspathUtils.isDirectory("io/transconnect/connector/test/reference/ClasspathUtils/test.xml"),
                "A regular file is not a directory");
    }

    /**
     * Tests that a non-existing resource is not recognized as a directory.
     */
    @Test
    public void testIsDirectoryNotExisting() {
        assertFalse(
                ClasspathUtils.isDirectory("io/transconnect/connector/test/reference/does-not-exist"),
                "A missing resource is not a directory");
    }

    /**
     * Tests that a resource that is not available via the file protocol is not recognized as a directory.
     */
    @Test
    public void testIsDirectoryNoFileProtocol() throws IOException {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoaderMock = mock(ClassLoader.class);
        when(classLoaderMock.getResource(any())).thenReturn(new URL("jar:file:/some/file.jar!/folder"));

        try {
            Thread.currentThread().setContextClassLoader(classLoaderMock);
            assertFalse(
                    ClasspathUtils.isDirectory("some/folder"),
                    "A resource inside a JAR is not recognized as a directory");
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    /**
     * Tests that an invalid resource URL (URISyntaxException) is not recognized as a directory.
     */
    @Test
    public void testIsDirectoryInvalidUrl() throws IOException {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoaderMock = mock(ClassLoader.class);
        // the space in the path makes url.toURI() throw a URISyntaxException
        when(classLoaderMock.getResource(any())).thenReturn(new URL("file:/some/invalid path"));

        try {
            Thread.currentThread().setContextClassLoader(classLoaderMock);
            assertFalse(
                    ClasspathUtils.isDirectory("some/invalid/path"),
                    "An invalid resource URL is not recognized as a directory");
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }
}
