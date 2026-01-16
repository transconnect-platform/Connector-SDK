/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api.property.validator;

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RangeValidatorConfig validator configuration for numeric validation.
 */
@Getter
@NoArgsConstructor
public class RangeValidatorConfig implements IValidatorTypeConfig {

    /**
     * Minimum allowed value.
     */
    private Long min;

    /**
     * Maximum allowed value.
     */
    private Long max;

    /**
     * Multiple range validation support.
     * Each pair represents an inclusive range [min, max]
     */
    private Map<Long, Long> ranges;

    /**
     * Returns true if this property is configured toi use ranges.
     * @return true if using multiple ranges instead of single min/max
     */
    public boolean isUsingRanges() {
        return ranges != null && !ranges.isEmpty();
    }
}
