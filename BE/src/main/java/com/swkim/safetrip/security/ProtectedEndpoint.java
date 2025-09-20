package com.swkim.safetrip.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@AllArgsConstructor
public enum ProtectedEndpoint {
    USER_REPORTS("/user-reports", HttpMethod.POST),
    ME("/users/me", HttpMethod.GET);

    @Getter
    private final String path;

    private final HttpMethod method;

    public String getMethod() {
        return method.toString();
    }
}