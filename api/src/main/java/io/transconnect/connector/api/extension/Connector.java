/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as connector.
 * The annotation can be used to specify the extensions that should be applied to the connector.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Connector {
    /**
     * The extensions that should be applied to the connector.
     *
     * @return the extensions
     */
    Extension[] extensions() default {};
}
