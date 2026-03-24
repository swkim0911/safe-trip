package com.swkim.safetrip.controller;

import com.swkim.safetrip.dto.request.ReportInaccuracyRequest;
import com.swkim.safetrip.dto.response.ReportInaccuracyItem;
import com.swkim.safetrip.global.response.ApiResult;
import com.swkim.safetrip.security.CustomUserDetails;
import com.swkim.safetrip.service.ReportInaccuracyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportInaccuracyController {

    private final ReportInaccuracyService reportInaccuracyService;

    @Operation(summary = "AI 리포트 오류 신고", description = "AI Bot이 수집한 리포트의 오류를 신고합니다", security = @SecurityRequirement(name = "BearerAuth"))
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/external-reports/{reportId}/inaccuracies")
    public ApiResult<Void> submitInaccuracy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId,
            @RequestBody @Valid ReportInaccuracyRequest request) {
        reportInaccuracyService.submit(reportId, userDetails.getUsername(), request);
        return ApiResult.of(HttpStatus.CREATED.value(), "Inaccuracy report submitted", null);
    }

    @Operation(summary = "내 신고 여부 조회", description = "해당 AI 리포트에 대해 이미 신고했는지 확인합니다", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/v1/external-reports/{reportId}/inaccuracies/my")
    public ApiResult<Boolean> hasSubmitted(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reportId) {
        boolean submitted = reportInaccuracyService.hasSubmitted(reportId, userDetails.getUsername());
        return ApiResult.of(HttpStatus.OK.value(), "Inaccuracy submission status retrieved", submitted);
    }

    @Operation(summary = "[Admin] 오류 신고 목록 조회", description = "신고된 AI 리포트 오류 목록을 조회합니다 (ADMIN 전용)", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/v1/admin/report-inaccuracies")
    public ApiResult<List<ReportInaccuracyItem>> getInaccuracyReports() {
        List<ReportInaccuracyItem> items = reportInaccuracyService.getInaccuracyReports();
        return ApiResult.of(HttpStatus.OK.value(), "Inaccuracy reports retrieved", items);
    }
}
