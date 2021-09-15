package com.tick42.quicksilver.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tick42.quicksilver.exceptions.*;
import org.kohsuke.github.GHException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler
    ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        String model = Arrays.stream(e.getMessage().split("[ .]+"))
                .filter(s -> s.equals("UserModel") || s.equals("File") || s.equals("Settings")
                        || s.equals("GitHub")).findFirst().orElse("Entity");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(model + " not found.");
    }

    @ExceptionHandler
    ResponseEntity<String> handleInvalidInputException(InvalidInputException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleFileFormatException(FileFormatException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        Map<String, String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage, (existing, replacement) -> existing, HashMap::new));

        try {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(objectMapper.writeValueAsString(errors));
        } catch (JsonProcessingException ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());
        }
    }

    @ExceptionHandler
    ResponseEntity<String> disabledUserException(DisabledUserException e){
        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleBlockedUserException(BlockedUserException e){
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler
    ResponseEntity<String> handleGHException(GHException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }
}
