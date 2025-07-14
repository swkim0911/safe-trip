package com.swkim.safetrip.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;


@Getter
public class GeneralAuthenticationException extends AuthenticationException {
    private final Error error;

    public GeneralAuthenticationException(Error error) {
        super(error.getMessage());
        this.error = error;
    }
}
