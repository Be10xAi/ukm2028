package com.tech10x.ukm.utils;

import com.tech10x.ukm.dto.response.GenericResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
@Component
public class ResponseUtil {
    public static <T> GenericResponse<T> success(List<T> data) {
        return GenericResponse.<T>builder()
                .data(data)
                .message("Success")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static <T> GenericResponse<T> success(String message) {
        return GenericResponse.<T>builder()
                .message(message)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> GenericResponse<T> success(List<T> data, String message) {
        return GenericResponse.<T>builder()
                .data(data)
                .message(message)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> GenericResponse<T> error(String message) {
        return GenericResponse.<T>builder()
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
