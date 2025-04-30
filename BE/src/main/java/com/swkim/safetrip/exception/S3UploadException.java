package com.swkim.safetrip.exception;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class S3UploadException extends GeneralException {

    public S3UploadException(){
        super(Error.REPORT_NOT_FOUND_ERROR);
    }


    public S3UploadException(Error error) {
        super(error);
    }
}
