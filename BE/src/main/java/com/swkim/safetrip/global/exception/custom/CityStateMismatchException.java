package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class CityStateMismatchException extends GeneralException {

    public CityStateMismatchException() {
        super(Error.STATE_COUNTRY_MISMATCH_ERROR);
    }
}
