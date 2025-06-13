package com.swkim.safetrip.exception;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class DuplicateUserEmailException extends GeneralException {

    public DuplicateUserEmailException() {
        super(Error.DUPLICATE_USER_EMAIL_ERROR);
    }
}
