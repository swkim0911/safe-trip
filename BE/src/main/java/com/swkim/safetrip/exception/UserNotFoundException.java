package com.swkim.safetrip.exception;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class UserNotFoundException extends GeneralException {

    public UserNotFoundException() {
        super(Error.USER_NOT_FOUND_EXCEPTION);
    }
}
