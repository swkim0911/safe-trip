package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class RefreshTokenReuseDetectedException extends GeneralException {

    public RefreshTokenReuseDetectedException() {
        super(Error.REFRESH_TOKEN_REUSE_DETECTED_ERROR);
    }
}
