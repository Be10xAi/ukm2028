package com.tech10x.ukm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {
    private boolean success;
    private String message;
    private List<T> data;
    private LocalDateTime timestamp;
}
