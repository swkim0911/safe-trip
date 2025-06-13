package com.swkim.safetrip.exception;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class CoordinatesNotValidException extends GeneralException {

    public CoordinatesNotValidException() {
        super(Error.COORDINATES_NOT_VALID_ERROR);
    }
}
