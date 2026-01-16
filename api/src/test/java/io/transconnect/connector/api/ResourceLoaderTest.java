/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public final class ResourceLoaderTest {

    private ClassLoader classLoader;

    @BeforeClass
    public void beforeClass() {
        classLoader = getClass().getClassLoader();
    }

    @DataProvider(name = "validResources")
    public Object[][] provideValidResources() {
        return new Object[][] {{URI.create("testfile.txt"), "Expected content of testfile.txt"}};
    }

    @Test(dataProvider = "validResources")
    public void testReadResource_Success(URI resourceUri, String expectedContent) throws IOException {
        byte[] content = ResourceLoader.readResource(classLoader, resourceUri);
        String actualContent = new String(content, StandardCharsets.UTF_8);
        assertEquals(actualContent.trim(), expectedContent);
    }

    @Test
    public void testReadResource_FileNotFound() {
        URI invalidUri = URI.create("nonexistent.txt");
        assertThrows(IOException.class, () -> ResourceLoader.readResource(classLoader, invalidUri));
    }
}
