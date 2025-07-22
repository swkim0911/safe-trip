package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.DuplicateCheckResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력하여 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일 또는 닉네임으로 인한 회원가입 실패")
    })
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Long> signup(@RequestBody @Valid UserSignUpRequest signUpRequest) {
        Long userId = userService.signup(signUpRequest);

        return ApiResult.of(HttpStatus.CREATED.value(), "Your membership has been registered.", userId);
    }

    @Operation(summary = "이메일 중복 체크", description = "회원가입 전에, 이메일(아이디) 중복 체크를 진행합니다")
    @GetMapping
    public ApiResult<DuplicateCheckResponse> checkEmailDuplicate(@RequestParam String email) {
        return ApiResult.of(HttpStatus.OK.value(), "", userService.checkEmailDuplicate(email));
    }

}
