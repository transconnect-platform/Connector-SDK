/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RegexValidatorConfig validator configuration for string pattern validation.
 */
@Getter
@NoArgsConstructor
public class RegexValidatorConfig implements IValidatorTypeConfig {

    /**
     * Regular expression pattern to validate against.
     */
    private String pattern;
}
