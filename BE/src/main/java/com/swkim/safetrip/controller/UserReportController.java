package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportSaveRequest;
import com.swkim.safetrip.dto.response.UserReportDetailResponse;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.UserReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user-reports")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportService userReportService;

    @Operation(summary = "글 등록", description = "새로운 게시글을 작성하여 서버에 등록합니다", security = @SecurityRequirement(name = "BearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResult<Long> createReport(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart @Valid ReportSaveRequest request, @RequestPart(required = false) List<MultipartFile> images) {
        String email = userDetails.getUsername();
        Long id = userReportService.saveUserReport(email, request, images);
        return ApiResult.of(HttpStatus.CREATED.value(), "User report registration successful", id);
    }

    @Operation(summary = "특정 유저 리포트 조회", description = "특정 유저 리포트를 아이디로 조회합니다")
    @GetMapping(value = "/{reportId}")
    public ApiResult<UserReportDetailResponse> getUserReport(@PathVariable Long reportId) {
        UserReportDetailResponse report = userReportService.getUserReport(reportId);
        return ApiResult.of(HttpStatus.OK.value(), "User report retrieved", report);
    }
}
