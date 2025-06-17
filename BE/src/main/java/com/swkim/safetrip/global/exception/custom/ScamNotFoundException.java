package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class ScamNotFoundException extends GeneralException {

    public ScamNotFoundException() {
        super(Error.SCAM_NOT_FOUND_ERROR);
    }
}
