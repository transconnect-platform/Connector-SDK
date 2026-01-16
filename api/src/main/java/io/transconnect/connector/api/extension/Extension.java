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
     * TODO pherklotz: This could be simplified. We could remove this
     * annotation and use a list of extension classes directly. The
     * current approach would allow us to define additional Extension
     * parameter (e.g. a supported OAuth configuration, order of properties) in the future.
     * @return the class of the extension
     */
    Class<? extends ConnectorExtension> value();
}
