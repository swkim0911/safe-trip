package com.swkim.safetrip.global.exception.handler;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;
import com.swkim.safetrip.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swkim.safetrip.global.exception.Error.METHOD_ARGUMENT_NOT_VALID_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> invalidRequestHandler(MethodArgumentNotValidException e){
        log.error("error", e);
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = e.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            String field = fieldError.getField();
            String defaultMessage = fieldError.getDefaultMessage();
            map.put(field, defaultMessage);
        }
        return ResponseEntity
                .status(e.getStatusCode().value())
                .body(ApiResponse.of(METHOD_ARGUMENT_NOT_VALID_ERROR.getStatusCode(), METHOD_ARGUMENT_NOT_VALID_ERROR.getMessage(), map));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Object>> generalExceptionHandler(GeneralException e) {
        log.error("error", e);

        Error error = e.getError();

        return ResponseEntity
                .status(error.getStatusCode())
                .body(ApiResponse.of(error.getStatusCode(), error.getMessage(), "Request Failed"));
    }

}
