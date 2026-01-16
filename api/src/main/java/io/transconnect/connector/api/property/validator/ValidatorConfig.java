/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Validator configuration for a connector property.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class ValidatorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * type of validator.
     */
    @NonNull
    private ValidatorType type;

    /**
     * configuration for this validator.
     */
    private IValidatorTypeConfig config;

    /**
     * Validates that the configuration is complete and consistent.
     *
     * @throws IllegalStateException if the configuration is invalid
     */
    public void validate() {

        if (type.getConfigClass() != null && config == null) {
            throw new IllegalStateException(String.format(
                    "Validator type %s requires configuration of type %s",
                    type, type.getConfigClass().getSimpleName()));
        }
    }
}
