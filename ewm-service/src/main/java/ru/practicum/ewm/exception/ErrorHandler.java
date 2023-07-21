package ru.practicum.ewm.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestControllerAdvice("ru.practicum.ewm")
public class ErrorHandler {

    @ExceptionHandler({UserNotFoundException.class, CategoryNotFoundException.class, EventNotFountException.class,
            CompilationNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorApi handleNotFoundException(final RuntimeException e) {
        log.info("Получен статус 404 Not found {}", e.getMessage(), e);
        return ErrorApi.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("The required object was not found.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(String.valueOf(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler({CategoryIsNotEmptyException.class, NotMetConditionsException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorApi constraint(final RuntimeException e) {
        log.info("Получен статус 409 Conflict {}", e.getMessage(), e);
        return ErrorApi.builder()
                .status(HttpStatus.CONFLICT)
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(String.valueOf(LocalDateTime.now()))
                .build();
    }
//
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.FORBIDDEN)
//    public ErrorApi handleNotEnoughRightsException(final NotEnoughRightsException e) {
//        log.info("Получен статус 403 Forbidden {}", e.getMessage(), e);
//        return new ErrorApi(e.getMessage());
//    }
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ErrorApi handleBadRequestConflictException(final ConstraintViolationException e) {
//        log.info("409 CONFLICT {}", e.getMessage());
//        return new ErrorApi(HttpStatus.CONFLICT, "Integrity constraint has been violated.", e.getMessage(), Collections.emptyList());
//    }


    //**
    //* MethodArgumentNotValidException валидация полей с помощью аннотаций
    //*
    //**
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class,
            MethodArgumentTypeMismatchException.class, NumberFormatException.class,
            MissingServletRequestParameterException.class}) //{ValidationException.class, ,IllegalArgumentException.class}
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorApi handleBadRequestException(final Exception e) {
        log.info("400 Bad Request {}", e.getMessage());
        return ErrorApi.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .errors(Collections.emptyList())
                .timestamp(String.valueOf(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorApi handleUnexpectedException(final Throwable e) {
        log.error("Error", e);
        StringWriter out = new StringWriter();
        e.printStackTrace(new PrintWriter(out));
        String stackTrace = out.toString();
        return ErrorApi.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Something went wrong.")
                .message(e.getMessage())
                .errors(Collections.singletonList(stackTrace))
                .timestamp(String.valueOf(LocalDateTime.now()))
                .build();
    }

}
