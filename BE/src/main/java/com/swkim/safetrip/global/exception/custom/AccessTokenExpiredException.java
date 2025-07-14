package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import org.springframework.security.core.AuthenticationException;

public class AccessTokenExpiredException extends AuthenticationException {

    public AccessTokenExpiredException() {
        super(Error.ACCESS_TOKEN_EXPIRED_ERROR.getMessage());
        this.error = Error.ACCESS_TOKEN_EXPIRED_ERROR;
    }
}
