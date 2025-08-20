package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class CountryNotFoundException extends GeneralException {

    public CountryNotFoundException() {
        super(Error.COUNTRY_NOT_FOUND_ERROR);
    }
}
