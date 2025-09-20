package com.swkim.safetrip.mapper;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.entity.User;
import com.swkim.safetrip.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
public class UserMapper {

    public static User toUser(UserSignUpRequest signUpRequest) {

        return User.builder()
                .email(signUpRequest.email())
                .password(signUpRequest.password())
                .nickname(signUpRequest.nickname())
                .role(Role.USER)
                .build();
    }

}
