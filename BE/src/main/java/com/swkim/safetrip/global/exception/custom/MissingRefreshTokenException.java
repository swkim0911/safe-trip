package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class MissingRefreshTokenException extends GeneralException {

    public MissingRefreshTokenException() {
        super(Error.MISSING_REFRESH_TOKEN_ERROR);
    }
}
