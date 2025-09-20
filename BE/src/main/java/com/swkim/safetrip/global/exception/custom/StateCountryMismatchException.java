package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class StateCountryMismatchException extends GeneralException {

    public StateCountryMismatchException() {
        super(Error.STATE_COUNTRY_MISMATCH_ERROR);
    }
}
