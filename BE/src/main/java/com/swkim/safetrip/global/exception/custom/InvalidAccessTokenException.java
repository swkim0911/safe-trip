package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralAuthenticationException;

public class InvalidAccessTokenException extends GeneralAuthenticationException {

    public InvalidAccessTokenException() {
        super(Error.INVALID_ACCESS_TOKEN_ERROR);
    }
}
