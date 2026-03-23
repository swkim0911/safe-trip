package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class ForbiddenReportAccessException extends GeneralException {

    public ForbiddenReportAccessException() {
        super(Error.FORBIDDEN_REPORT_ACCESS_ERROR);
    }
}
