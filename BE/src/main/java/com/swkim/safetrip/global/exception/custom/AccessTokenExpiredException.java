package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralAuthenticationException;

public class AccessTokenExpiredException extends GeneralAuthenticationException {

    public AccessTokenExpiredException() {
        super(Error.ACCESS_TOKEN_EXPIRED_ERROR);
    }
}
