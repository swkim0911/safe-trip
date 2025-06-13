package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;

public class UserMapper {

    public static User toUser(UserSignUpRequest signUpRequest) {

        return User.builder()
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .nickname(signUpRequest.getNickname())
                .build();
    }

}
