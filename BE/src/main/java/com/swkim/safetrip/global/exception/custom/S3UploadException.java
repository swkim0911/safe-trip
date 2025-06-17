package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class S3UploadException extends GeneralException {

    public S3UploadException(){
        super(Error.REPORT_NOT_FOUND_ERROR);
    }
}
