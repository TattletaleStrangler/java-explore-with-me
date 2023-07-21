package ru.practicum.ewm.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class ErrorApi {
    private final List<String> errors;

    private final String message;

    private final String reason;

    private final HttpStatus status;

    private final String timestamp;

    public ErrorApi(HttpStatus status, String reason, String message, List<String> errors) {
        this.errors = errors;
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.timestamp = String.valueOf(LocalDateTime.now());
    }

}