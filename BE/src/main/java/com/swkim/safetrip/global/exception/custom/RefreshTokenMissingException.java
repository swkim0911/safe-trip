package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class RefreshTokenMissingException extends GeneralException {

    public RefreshTokenMissingException() {
        super(Error.REFRESH_TOKEN_MISSING_ERROR);
    }
}
