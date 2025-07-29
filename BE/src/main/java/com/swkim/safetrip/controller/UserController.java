package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.UserInfoResponse;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

        return ApiResult.of(HttpStatus.CREATED.value(), "membership has been registered", userId);
    }

    @Operation(summary = "이메일 사용 가능 여부 확인", description = "입력한 이메일이 형식에 맞고, 중복 여부를 확인합니다.")
    @GetMapping("/users/validate-email")
    public ApiResult<ValidationResponse> validateEmail(@RequestParam String email) {
        return ApiResult.of(HttpStatus.OK.value(), "completed duplicate check of email", userService.validateEmail(email));
    }

    @Operation(summary = "닉네임 사용 가능 여부 확인", description = "입력한 닉네임이 형식에 맞고, 중복 여부를 확인합니다.")
    @GetMapping("users/validate-nickname")
    public ApiResult<ValidationResponse> validateNickname(@RequestParam String nickname) {
        return ApiResult.of(HttpStatus.OK.value(), "completed duplicate check of nickname", userService.validateNickname(nickname));
    }

    @Operation(summary = "사용자 정보 조회", description = "로그인 후, 사용자 정보를 조회합니다")
    @GetMapping("/me")
    public ApiResult<UserInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .nickname(userDetails.getNickname())
                .build();

        return ApiResult.of(HttpStatus.OK.value(), "User information has been successfully retrieved", userInfoResponse);
    }

}
