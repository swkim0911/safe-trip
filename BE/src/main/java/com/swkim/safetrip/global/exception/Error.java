package com.swkim.safetrip.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Error {

    UN_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
