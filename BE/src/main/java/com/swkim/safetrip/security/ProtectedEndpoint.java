package com.swkim.safetrip.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@AllArgsConstructor
public enum ProtectedEndpoint {
    USER_REPORTS("/v1/user-reports", HttpMethod.POST),       // user report 등록 요청
    USER_REPORT_UPDATE("/v1/user-reports/*", HttpMethod.PUT),    // user report 수정
    USER_REPORT_DELETE("/v1/user-reports/*", HttpMethod.DELETE), // user report 삭제
    ME("/v1/users/me", HttpMethod.GET),                       // user 개인 정보 요청
    ME_REPORTS("/v1/users/me/reports", HttpMethod.GET),       // 내 리포트 목록
    ME_NICKNAME("/v1/users/me/nickname", HttpMethod.PATCH),
    REPORT_INACCURACY("/v1/external-reports/*/inaccuracies", HttpMethod.POST),
    MY_REPORT_INACCURACY("/v1/external-reports/*/inaccuracies/my", HttpMethod.GET);   // 닉네임 수정

    @Getter
    private final String path;

    private final HttpMethod method;

    public String getMethod() {
        return method.toString();
    }
}