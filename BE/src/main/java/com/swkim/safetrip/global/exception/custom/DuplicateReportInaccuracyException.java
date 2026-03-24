package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class DuplicateReportInaccuracyException extends GeneralException {

    public DuplicateReportInaccuracyException() {
        super(Error.DUPLICATE_REPORT_INACCURACY_ERROR);
    }
}
