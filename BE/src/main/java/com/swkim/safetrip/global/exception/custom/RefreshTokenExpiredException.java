package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class RefreshTokenExpiredException extends GeneralException {

    public RefreshTokenExpiredException() {
        super(Error.REFRESH_TOKEN_EXPIRED_ERROR);
    }
}
