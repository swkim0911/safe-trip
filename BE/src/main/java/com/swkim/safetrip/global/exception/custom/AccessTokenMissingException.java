package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralAuthenticationException;

public class AccessTokenMissingException extends GeneralAuthenticationException {

    public AccessTokenMissingException() {
        super(Error.ACCESS_TOKEN_MISSING_ERROR);
    }
}
