package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.global.response.ApiResponse;
import com.swkim.safetrip.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> signup(@RequestBody @Valid UserSignUpRequest signUpRequest) {
        Long userId = userService.enroll(signUpRequest);

        return ApiResponse.of(HttpStatus.CREATED.value(), "회원가입이 완료되었습니다.", userId);
    }
}
