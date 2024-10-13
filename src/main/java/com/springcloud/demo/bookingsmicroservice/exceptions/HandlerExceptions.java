package com.springcloud.demo.bookingsmicroservice.exceptions;

import com.springcloud.demo.bookingsmicroservice.exceptions.dto.ErrorResponseDTO;
import com.springcloud.demo.bookingsmicroservice.monitoring.TracingExceptions;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class HandlerExceptions {

    private final TracingExceptions tracingExceptions;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundExceptions(NotFoundException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDTO handleForbiddenExceptions(ForbiddenException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<String> errors = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).toList();

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    @ExceptionHandler({BadRequestException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleBadRequestException(Exception e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    }

    /**
     * handle errors on validation on PathVariables
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodValidationException(HandlerMethodValidationException e){
        List<String> errors = new ArrayList<>();

        e.getAllValidationResults().forEach(err -> {
            err.getResolvableErrors().forEach(resolvable -> {
                String errorMessage = "";
                if(Objects.requireNonNull(resolvable.getCodes())[0] != null){
                    errorMessage += resolvable.getCodes()[0].split("\\.")[resolvable.getCodes().length - 2] + " ";
                }
                errorMessage += resolvable.getDefaultMessage();
                errors.add(errorMessage);
            });
        });

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .errors(errors)
                .build();
    }

    /**
     * Handle errors when not exist field in body request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        String message = e.getMessage().split(":")[0];

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .build();
    }
}
