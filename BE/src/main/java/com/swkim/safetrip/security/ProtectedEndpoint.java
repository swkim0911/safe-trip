package com.swkim.safetrip.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@AllArgsConstructor
public enum ProtectedEndpoint {
    USER_REPORTS("/v1/user-reports", HttpMethod.POST), // user report 등록 요청
    ME("/v1/users/me", HttpMethod.GET); // user 개인 정보 요청

    @Getter
    private final String path;

    private final HttpMethod method;

    public String getMethod() {
        return method.toString();
    }
}