package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import org.springframework.security.core.AuthenticationException;

public class InvalidAccessTokenException extends AuthenticationException {

    public InvalidAccessTokenException() {
        super(Error.INVALID_ACCESS_TOKEN_ERROR.getMessage());
    }
}
