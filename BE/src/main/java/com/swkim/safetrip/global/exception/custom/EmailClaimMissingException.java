package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralAuthenticationException;

public class EmailClaimMissingException extends GeneralAuthenticationException {

    public EmailClaimMissingException() {
        super(Error.EMAIL_CLAIM_MISSING_ERROR);
    }
}
