package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class CityNotFoundException extends GeneralException {

    public CityNotFoundException() {
        super(Error.CITY_NOT_FOUND_ERROR);
    }
}
