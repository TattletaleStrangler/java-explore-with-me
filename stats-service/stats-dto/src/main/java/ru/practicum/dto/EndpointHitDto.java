package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
public class EndpointHitDto {
    private Long id;

    @NotEmpty
    private String app;

    @NotEmpty
    private String uri;

    @NotEmpty
    private  String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
