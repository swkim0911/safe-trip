package com.swkim.safetrip.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum Error {

    METHOD_ARGUMENT_NOT_VALID_ERROR(HttpStatus.BAD_REQUEST.value(), "잘못된 요청입니다."),
    UN_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED.value(), "로그인이 필요합니다."),
    COORDINATES_NOT_VALID_ERROR(HttpStatus.BAD_REQUEST.value(), "잘못된 위치 정보 입력입니다."),
    REPORT_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND.value(), "report를 찾을 수 없습니다."),
    SCAM_NOT_FOUND_ERROR(HttpStatus.NOT_FOUND.value(), "report를 찾을 수 없습니다."),
    S3_UPLOAD_ERROR(HttpStatus.BAD_REQUEST.value(), "s3 버킷에 이미지를 업로드할 수 없습니다."),
    DUPLICATE_USER_EMAIL_ERROR(HttpStatus.BAD_REQUEST.value(), "This email is already in use"),
    DUPLICATE_USER_NICKNAME_ERROR(HttpStatus.BAD_REQUEST.value(), "This nickname is already in use"),
    USER_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND.value(), "User not found by email."),

    REFRESH_TOKEN_MISSING_ERROR(HttpStatus.BAD_REQUEST.value(), "Refresh token is empty"),
    REFRESH_TOKEN_EXPIRED_ERROR(HttpStatus.UNAUTHORIZED.value(), "Refresh token is expired"),
    INVALID_REFRESH_TOKEN_ERROR(HttpStatus.UNAUTHORIZED.value(), "Refresh token is invalid"),
    REFRESH_TOKEN_REUSE_DETECTED_ERROR(HttpStatus.UNAUTHORIZED.value(), "Refresh token reuse detected"),
    ACCESS_TOKEN_MISSING_ERROR(HttpStatus.UNAUTHORIZED.value(), "Access token is missing"),
    ACCESS_TOKEN_EXPIRED_ERROR(40101, "Access token is expired"),
    EMAIL_CLAIM_MISSING_ERROR(HttpStatus.UNAUTHORIZED.value(), "Email claim is missing"),
    INVALID_ACCESS_TOKEN_ERROR(HttpStatus.UNAUTHORIZED.value(), "Access token is invalid"),

    TOKEN_HASHING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed token hashing");

    private final int statusCode;
    private final String message;
}
