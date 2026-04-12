package com.swkim.safetrip.global.exception.handler;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;
import com.swkim.safetrip.global.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swkim.safetrip.global.exception.Error.METHOD_ARGUMENT_NOT_VALID_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> invalidRequestHandler(MethodArgumentNotValidException e){
        log.warn("Validation failed: {}", e.getMessage());
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = e.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            map.put(field, defaultMessage);
        }
        return ResponseEntity
                .status(e.getStatusCode().value())
                .body(ApiResult.of(METHOD_ARGUMENT_NOT_VALID_ERROR.getStatusCode(), METHOD_ARGUMENT_NOT_VALID_ERROR.getMessage(), map));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResult<Object>> generalExceptionHandler(GeneralException e) {
        Error error = e.getError();
        if (error.getStatusCode() >= 500) {
            log.error("Server error: {}", error.getMessage(), e);
        } else {
            log.warn("Client error [{}]: {}", error.getStatusCode(), error.getMessage());
        }

        return ResponseEntity
                .status(error.getStatusCode())
                .body(ApiResult.of(error.getStatusCode(), error.getMessage(), null));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResult<String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("error", ex);

        return ApiResult.of(404, "API does not exist", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> unexpectedExceptionHandler(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.of(500, "Internal Server Error", null));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiResult<String>> handleMissingCookie(MissingRequestCookieException ex) {
        log.error("error", ex);

        String cookieName = ex.getCookieName();
        boolean isAuthCookie = "refreshToken".equals(cookieName);

        HttpStatus status = isAuthCookie ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;

        String message = isAuthCookie
                ? "Authentication cookie (refreshToken) is missing."
                : String.format("Required cookie (%s) is missing from the request.", cookieName);

        ApiResult<String> response = ApiResult.of(status.value(), message, null);

        return ResponseEntity.status(status).body(response);
    }
}
