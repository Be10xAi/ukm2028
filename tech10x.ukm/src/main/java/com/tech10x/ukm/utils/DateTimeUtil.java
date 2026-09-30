package com.tech10x.ukm.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DateTimeUtil {

    /** The platform runs on Indian time (see spring.jackson.time-zone). */
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public static LocalDate todayIst() {
        return LocalDate.now(IST);
    }

}
