package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class TokenHashingException extends GeneralException {

    public TokenHashingException() {
        super(Error.TOKEN_HASHING_ERROR);
    }
}
