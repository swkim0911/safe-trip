package com.swkim.safetrip.global.exception.custom;

import com.swkim.safetrip.global.exception.Error;
import com.swkim.safetrip.global.exception.GeneralException;

public class CommentNotFoundException extends GeneralException {

    public CommentNotFoundException() {
        super(Error.COMMENT_NOT_FOUND_ERROR);
    }
}
