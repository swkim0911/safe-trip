package com.swkim.safetrip.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Error {

    // 400
    Method_Argument_NotValid_ERROR(HttpStatus.BAD_REQUEST.value(), "잘못된 입력입니다."),

    // 401
    UN_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다.");

    private final int statusCode;
    private final String message;
}
