package com.swkim.safetrip.controller;

import com.swkim.safetrip.global.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-reports")
@RequiredArgsConstructor
public class ExternalReportController {

    private final ExternalReportService externalReportService;

    @Operation(summary = "외부 리포트 상세 조회", description = "외부 리포트 상세 정보를 아이디로 조회합니다")
    @GetMapping(value = "/{reportId}")
    public ApiResult<ExternalReportDetailResponse> getExternalReport(@PathVariable Long reportId) {
        ExternalReportDetailResponse report = externalReportService.getExternalReport(reportId);
        return ApiResult.of(HttpStatus.OK.value(), "External report retrieved", report);
    }
}
