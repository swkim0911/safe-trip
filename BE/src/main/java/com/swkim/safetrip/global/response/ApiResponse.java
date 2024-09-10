package com.swkim.safetrip.global.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;

    private String message;

    private T result;

    public static <T> ApiResponse<T> of(int code, String message, T result){
        return new ApiResponse<>(code, message, result);
    }

}
