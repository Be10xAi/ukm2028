package com.tech10x.ukm.utils;

import java.util.regex.Pattern;

/**
 * Supplier-entered text (names, descriptions, policies) is shown publicly, so it is stored as
 * plain text: HTML tags and control characters are stripped on the way in. This is defence in
 * depth against stored XSS - the frontend must still render these fields as text, not HTML.
 */
public final class TextSanitizer {

    private static final Pattern TAGS = Pattern.compile("</?[A-Za-z!?][^>]{0,2000}>");
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private TextSanitizer() {
    }

    public static String plain(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = CONTROL.matcher(TAGS.matcher(value).replaceAll("")).replaceAll("").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
