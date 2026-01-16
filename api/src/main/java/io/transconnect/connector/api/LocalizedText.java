/*
 * (c) Copyright 2025 SQL Projekt AG. All rights reserved.
 */
package io.transconnect.connector.api;

import java.io.Serializable;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A text with a locale that specifies the message language.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class LocalizedText implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * the language of the text.
     */
    private Locale locale;

    /**
     * the text.
     */
    private String message;

    /**
     * Creates a new instance of LocalizedText with the specified locale and message.
     * @param locale the locale for the text
     * @param message the message to be localized
     * @return a new instance of LocalizedText
     */
    public static LocalizedText of(Locale locale, String message) {
        return LocalizedText.builder().locale(locale).message(message).build();
    }
}
