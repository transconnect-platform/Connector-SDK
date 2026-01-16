/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

import lombok.Getter;

/**
 * Types of validators available in the system.
 */
@Getter
public enum ValidatorType {

    /**
     * Used for string pattern validation using regular expressions.
     * This validator type can check if strings match specific patterns or formats.
     */
    REGEX(RegexValidatorConfig.class),

    /**
     * Used for numeric range validation.
     * This validator type checks if numbers fall within specified minimum and maximum bounds.
     * Previously known as NUMBER in earlier versions.
     */
    RANGE(RangeValidatorConfig.class),

    /**
     * Used for date and time format validation.
     * This validator type ensures dates and times conform to specified formats.
     */
    DATETIME(DateTimeValidatorConfig.class);

    /**
     * The configuration class associated with this validator type.
     */
    private final Class<? extends IValidatorTypeConfig> configClass;

    /**
     * Constructor for ValidatorType.
     *
     * @param configClass the configuration class for this validator type
     * @throws NullPointerException if configClass is null
     */
    ValidatorType(Class<? extends IValidatorTypeConfig> configClass) {

        if (configClass == null) {
            throw new NullPointerException("Configuration class cannot be null");
        }
        this.configClass = configClass;
    }
}
