package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import org.springframework.security.core.AuthenticationException;

public class AccessTokenMissingException extends AuthenticationException {

    public AccessTokenMissingException() {
        super(Error.ACCESS_TOKEN_MISSING_ERROR.getMessage());
    }
}
