package com.tech10x.ukm.utils;

import com.tech10x.ukm.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Room numbers are free text ("101", "A-12"), so they are normalised once and sorted "naturally". */
public final class RoomNumbers {

    private static final Pattern VALID = Pattern.compile("^[A-Z0-9][A-Z0-9\\-/]{0,19}$");
    private static final Pattern CHUNKS = Pattern.compile("\\d+|\\D+");

    /** 2 &lt; 10 &lt; 101, and A-2 &lt; A-10 (plain string order would put "10" before "2"). */
    public static final Comparator<String> NATURAL = RoomNumbers::compare;

    private RoomNumbers() {
    }

    /** Trims and upper-cases; rejects anything outside 1-20 characters of letters, digits, '-' and '/'. */
    public static String normalise(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (!VALID.matcher(value).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Room numbers must be 1-20 characters: letters, digits, '-' or '/'");
        }
        return value;
    }

    private static int compare(String a, String b) {
        Matcher ma = CHUNKS.matcher(a);
        Matcher mb = CHUNKS.matcher(b);
        while (ma.find() && mb.find()) {
            String x = ma.group();
            String y = mb.group();
            int c = Character.isDigit(x.charAt(0)) && Character.isDigit(y.charAt(0))
                    ? new BigInteger(x).compareTo(new BigInteger(y))
                    : x.compareTo(y);
            if (c != 0) {
                return c;
            }
        }
        int byLength = Integer.compare(a.length(), b.length());
        return byLength != 0 ? byLength : a.compareTo(b);
    }
}
