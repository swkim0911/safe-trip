package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.UpdateNicknameRequest;
import com.swkim.safetrip.dto.request.UserSignUpRequest;
import com.swkim.safetrip.dto.response.MyReportItem;
import com.swkim.safetrip.dto.response.UserInfoResponse;
import com.swkim.safetrip.dto.response.ValidationResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.UserReportService;
import com.swkim.safetrip.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserReportService userReportService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력하여 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일 또는 닉네임으로 인한 회원가입 실패")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<Long> signup(@RequestBody @Valid UserSignUpRequest signUpRequest) {
        Long userId = userService.signup(signUpRequest);

        return ApiResult.of(HttpStatus.CREATED.value(), "membership has been registered", userId);
    }

    @Operation(summary = "이메일 사용 가능 여부 확인", description = "입력한 이메일이 형식에 맞고, 중복 여부를 확인합니다.")
    @GetMapping("/validate-email")
    public ApiResult<ValidationResponse> validateEmail(@RequestParam String email) {
        return ApiResult.of(HttpStatus.OK.value(), "Complete email duplication check", userService.validateEmail(email));
    }

    @Operation(summary = "닉네임 사용 가능 여부 확인", description = "입력한 닉네임이 형식에 맞고, 중복 여부를 확인합니다.")
    @GetMapping("/validate-nickname")
    public ApiResult<ValidationResponse> validateNickname(@RequestParam String nickname) {
        return ApiResult.of(HttpStatus.OK.value(), "Complete nickname duplication check", userService.validateNickname(nickname));
    }

    @Operation(summary = "사용자 정보 조회", description = "로그인 후, 사용자 정보를 조회합니다")
    @GetMapping("/me")
    public ApiResult<UserInfoResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse userInfoResponse = UserInfoResponse.builder()
                .nickname(userDetails.getNickname())
                .role(userDetails.getRole().name())
                .build();

        return ApiResult.of(HttpStatus.OK.value(), "User information has been successfully retrieved", userInfoResponse);
    }

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공"),
            @ApiResponse(responseCode = "400", description = "이미 사용 중인 닉네임")
    })
    @PatchMapping("/me/nickname")
    public ApiResult<Void> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateNicknameRequest request) {
        userService.updateNickname(userDetails.getUsername(), request);

        return ApiResult.of(HttpStatus.OK.value(), "Nickname updated successfully", null);
    }

    @Operation(summary = "내 리포트 목록 조회", description = "로그인한 사용자가 작성한 리포트 목록을 조회합니다.")
    @GetMapping("/me/reports")
    public ApiResult<List<MyReportItem>> getMyReports(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<MyReportItem> reports = userReportService.getMyReports(userDetails.getUsername());
        return ApiResult.of(HttpStatus.OK.value(), "My reports retrieved", reports);
    }

    @Operation(summary = "회원 탈퇴", description = "계정과 관련된 모든 데이터를 삭제합니다.")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {
        userService.deleteAccount(userDetails.getUsername(), response);
    }

}
