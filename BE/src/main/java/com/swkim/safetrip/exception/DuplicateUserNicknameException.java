package com.swkim.safetrip.exception;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class DuplicateUserNicknameException extends GeneralException {

    public DuplicateUserNicknameException() {
        super(Error.DUPLICATE_USER_NICKNAME_ERROR);
    }
}
