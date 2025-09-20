package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class InvalidRefreshTokenException extends GeneralException {

    public InvalidRefreshTokenException() {
        super(Error.INVALID_REFRESH_TOKEN_ERROR);
    }
}
