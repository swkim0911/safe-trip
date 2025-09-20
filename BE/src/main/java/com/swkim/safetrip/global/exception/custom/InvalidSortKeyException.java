package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class InvalidSortKeyException extends GeneralException {

    public InvalidSortKeyException() {
        super(Error.INVALID_SORT_KEY_ERROR);
    }
}
