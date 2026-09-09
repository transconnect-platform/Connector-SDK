/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.test.reference;

import lombok.Getter;

/**
 * Represents an exception that occurs during reference test operations.
 * This class extends the standard {@link Exception} class and can be used to throw exceptions with detailed messages or nested causes.
 */
@Getter
public class ReferenceTestException extends Exception {

    /**
     * Enumeration of possible reference test errors.
     */
    public enum ReferenceError {
        /**
         * Error occurred while reading the well-formedness of the xml.
         */
        WELL_FORMED_READ,
        /**
         * Error occurred during test extraction process of the test part.
         */
        TEST_EXTRACTION,
        /**
         * No default namespace found during processing of the connector xsd.
         */
        NO_DEFAULT_NAMESPACE,
        /**
         * No XSI schema location found in the xml document.
         */
        NO_XSI_SCHEMA_LOCATION,
        /**
         * No namespace schema mapping found in schemaLocation.
         */
        NO_NAMESPACE_SCHEMA_MAPPING,
        /**
         * Error occurred during schema parsing.
         */
        INVALID_SCHEMA_PARSING,
        /**
         * Expected output is missing while validation the xml.
         */
        MISSING_OUTPUT,
        /**
         * Invalid payload received while validating the xml document.
         */
        INVALID_PAYLOAD,
        /**
         * Validation of the xml document failed.
         */
        VALIDATION_FAILED,
        /**
         * XSD file is missing in the schemaLocation.
         */
        MISSING_XSD_FILE,
        /**
         * The given XSD file in the schemaLocation is missing - on the classpath.
         */
        MISSING_XSD_FILE_ON_CLASSPATH,
        /**
         * The given XSD file in the schemaLocation is missing from relative path.
         */
        MISSING_XSD_FILE_ON_CLASSPATH_FROM_RELATIVE,
        /**
         * Unsupported protocol encountered for the schemaLocation file.
         */
        UNSUPPORTED_PROTOCOL,
        /**
         * Unknown error occurred.
         */
        UNKNOWN
    }

    /**
     * the category of the reference test error.
     *
     * @return the category of the reference test error
     */
    private final ReferenceError error;

    /**
     * Constructs a new exception with the specified error, detail message and cause.
     *
     * @param error The error category (which is saved for later retrieval by the {@link #getError()} method).
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param throwable The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value
     * is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ReferenceTestException(ReferenceError error, String message, Throwable throwable) {
        super(message, throwable);
        this.error = error;
    }

    /**
     * Constructs a new exception with the specified detail message and cause and the error category
     * {@link ReferenceError#UNKNOWN}.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param throwable The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value
     * is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ReferenceTestException(String message, Throwable throwable) {
        super(message, throwable);
        this.error = ReferenceError.UNKNOWN;
    }

    /**
     * Constructs a new exception with the specified error and detail message.
     *
     * @param error The error category (which is saved for later retrieval by the {@link #getError()} method).
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ReferenceTestException(ReferenceError error, String message) {
        super(message);
        this.error = error;
    }

    /**
     * Constructs a new exception with the specified detail message and the error category
     * {@link ReferenceError#UNKNOWN}.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     */
    public ReferenceTestException(String message) {
        super(message);
        this.error = ReferenceError.UNKNOWN;
    }
}
