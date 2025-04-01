package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.SignUpRequest;
import com.swkim.safetrip.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public Long signup(@Valid SignUpRequest signUpRequest) {
        return userService.enroll(signUpRequest);
    }
}
