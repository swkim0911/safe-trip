package com.swkim.safetrip.global.exception;

import com.swkim.safetrip.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.swkim.safetrip.global.exception.Error.Method_Argument_NotValid_ERROR;

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
                .body(ApiResponse.of(Method_Argument_NotValid_ERROR.getStatusCode(), Method_Argument_NotValid_ERROR.getMessage(), map));
    }

}
