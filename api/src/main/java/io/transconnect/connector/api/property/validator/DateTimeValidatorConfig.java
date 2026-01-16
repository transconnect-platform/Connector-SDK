/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DateTime validator configuration.
 */
@Getter
@NoArgsConstructor
public class DateTimeValidatorConfig implements IValidatorTypeConfig {

    /**
     * Date/time format pattern (e.g., "yyyy-MM-dd HH:mm:ss").
     */
    private String format;
}
