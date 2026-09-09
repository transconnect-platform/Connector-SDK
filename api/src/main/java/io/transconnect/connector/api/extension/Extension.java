/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.extension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to apply a connector extension to a connector.
 *
 * <p>This annotation is used to specify the class of the extension that should be used.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Extension {

    /**
     * The class of the extension.
     *
     * <p>The indirection through this annotation instead of a plain list of extension classes leaves
     * room for additional per-extension parameters, such as a supported OAuth configuration or the
     * order of the injected properties.</p>
     *
     * @return the class of the extension
     */
    Class<? extends ConnectorExtension> value();
}
