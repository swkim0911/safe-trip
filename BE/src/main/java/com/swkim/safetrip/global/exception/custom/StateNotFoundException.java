package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class StateNotFoundException extends GeneralException {

    public StateNotFoundException() {
        super(Error.STATE_NOT_FOUND_ERROR);
    }
}
