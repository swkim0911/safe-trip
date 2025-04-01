package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.SignUpRequest;
import com.swkim.safetrip.entity.User;

public class UserMapper {

    public static User toUser(SignUpRequest signUpRequest) {

        return User.builder()
                .username(signUpRequest.getUsername())
                .password(signUpRequest.getPassword())
                .name(signUpRequest.getName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .email(signUpRequest.getEmail())
                .build();
    }

}
