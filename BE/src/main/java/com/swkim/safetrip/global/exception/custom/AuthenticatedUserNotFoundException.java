package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class AuthenticatedUserNotFoundException extends GeneralException {

    public AuthenticatedUserNotFoundException() {
        super(Error.AUTHENTICATED_USER_NOT_FOUND_ERROR);
    }
}
