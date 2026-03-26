package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class InvalidParentCommentException extends GeneralException {

    public InvalidParentCommentException() {
        super(Error.INVALID_PARENT_COMMENT_ERROR);
    }
}
