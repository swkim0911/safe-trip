package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class ForbiddenCommentAccessException extends GeneralException {

    public ForbiddenCommentAccessException() {
        super(Error.FORBIDDEN_COMMENT_ACCESS_ERROR);
    }
}
