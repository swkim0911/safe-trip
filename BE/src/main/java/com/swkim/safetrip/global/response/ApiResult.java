package com.swkim.safetrip.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;

    private String message;

    private T result;

    public static <T> ApiResult<T> of(int code, String message, T result){
        return new ApiResult<>(code, message, result);
    }

}
