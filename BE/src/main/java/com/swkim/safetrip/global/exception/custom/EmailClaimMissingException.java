package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class EmailClaimMissingException extends GeneralException {

    public EmailClaimMissingException() {
        super(Error.EMAIL_CLAIM_MISSING_ERROR);
    }
}
