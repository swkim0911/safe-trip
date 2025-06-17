package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class ReportNotFoundException extends GeneralException {

    public ReportNotFoundException() {
        super(Error.REPORT_NOT_FOUND_ERROR);
    }
}
