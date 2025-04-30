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
    REPORT_NOT_FOUND_ERROR(HttpStatus.BAD_REQUEST.value(), "report를 찾을 수 없습니다."),
    SCAM_NOT_FOUND_ERROR(HttpStatus.BAD_REQUEST.value(), "report를 찾을 수 없습니다."),
    S3_UPLOAD_ERROR(HttpStatus.BAD_REQUEST.value(), "s3 버킷에 이미지를 업로드할 수 없습니다.");

    private final int statusCode;
    private final String message;
}
